package com.agh.polymorphia_backend.service.task.async_submission;

import com.agh.polymorphia_backend.model.gradable_event.subtypes.task.TaskSubmission;
import com.agh.polymorphia_backend.model.gradable_event.subtypes.task.TaskSubmissionResult;
import com.agh.polymorphia_backend.model.gradable_event.subtypes.task.TaskSubmissionStatus;
import com.agh.polymorphia_backend.model.gradable_event.subtypes.task.TaskTestCase;
import com.agh.polymorphia_backend.model.gradable_event.subtypes.task.TaskTestCaseStatus;
import com.agh.polymorphia_backend.repository.task.TaskSubmissionRepository;
import com.agh.polymorphia_backend.repository.task.TaskSubmissionResultRepository;
import com.agh.polymorphia_backend.repository.task.TaskTestCaseRepository;
import com.agh.polymorphia_backend.service.task.async_submission.config.TaskSubmissionProperties;
import com.agh.polymorphia_backend.service.task.dto.TaskSubmissionContext;
import com.agh.polymorphia_backend.service.task.dto.TaskTestCaseOutcome;
import com.agh.polymorphia_backend.service.task.dto.TaskTestCaseSpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskSubmissionPersistenceService {

    private static final String PROCESSING_ERROR_MESSAGE =
        "Wystąpił błąd podczas sprawdzania rozwiązania.";
    private static final String MAX_PROCESSING_ATTEMPTS_ERROR_MESSAGE =
        "Nie udało się sprawdzić rozwiązania. Skontaktuj się z prowadzącym.";

    private final TaskSubmissionRepository taskSubmissionRepository;
    private final TaskSubmissionResultRepository taskSubmissionResultRepository;
    private final TaskTestCaseRepository taskTestCaseRepository;
    private final TaskSubmissionScoreCalculator taskSubmissionScoreCalculator;
    private final TaskSubmissionProperties taskSubmissionProperties;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<TaskSubmissionContext> claimNextTaskSubmissions(int batchSize) {
        List<TaskSubmission> lockedTaskSubmissions =
            taskSubmissionRepository.findNextQueuedTaskSubmissionsWithLock(batchSize);

        if (lockedTaskSubmissions.isEmpty()) {
            return List.of();
        }

        List<Long> taskSubmissionIds = lockedTaskSubmissions.stream()
            .map(TaskSubmission::getId)
            .toList();

        List<TaskSubmission> taskSubmissions =
            taskSubmissionRepository.findAllWithTaskByIdIn(taskSubmissionIds);

        List<Long> taskIds = taskSubmissions.stream()
            .map(taskSubmission -> taskSubmission.getTask().getId())
            .distinct()
            .toList();

        Map<Long, List<TaskTestCase>> testCasesByTaskId =
            taskTestCaseRepository.findByTaskIdIn(taskIds).stream()
                .collect(Collectors.groupingBy(testCase -> testCase.getTask().getId()));

        Instant now = Instant.now();
        Instant lockedUntil = now.plusSeconds(taskSubmissionProperties.leaseDurationSeconds());

        List<TaskSubmissionContext> taskSubmissionContexts = new ArrayList<>();

        for (TaskSubmission taskSubmission : taskSubmissions) {
            taskSubmission.setStatus(TaskSubmissionStatus.RUNNING);
            taskSubmission.setStartedAt(now);
            taskSubmission.setLockedUntil(lockedUntil);
            taskSubmission.setProcessingAttempts(taskSubmission.getProcessingAttempts() + 1);
            taskSubmission.setLeaseToken(UUID.randomUUID());

            List<TaskTestCase> testCases =
                testCasesByTaskId.getOrDefault(taskSubmission.getTask().getId(), List.of());

            taskSubmissionContexts.add(TaskSubmissionContext.from(taskSubmission, testCases));
        }

        return taskSubmissionContexts;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveResultsAndComplete(
        TaskSubmissionContext taskSubmissionContext,
        List<TaskTestCaseOutcome> outcomes
    ) {
        int passedCount = 0;
        BigDecimal passedWeight = BigDecimal.ZERO;

        for (TaskTestCaseOutcome outcome : outcomes) {
            if (outcome.status() == TaskTestCaseStatus.PASSED) {
                passedCount++;
                passedWeight = passedWeight.add(outcome.weight());
            }
        }

        int totalCount = taskSubmissionContext.testCases().size();
        BigDecimal totalWeight = taskSubmissionContext.testCases().stream()
            .map(TaskTestCaseSpec::weight)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal scorePercentage = taskSubmissionScoreCalculator.calculateScorePercentage(
            taskSubmissionContext.gradingStrategy(),
            passedCount,
            totalCount,
            passedWeight,
            totalWeight
        );

        int updatedRows = taskSubmissionRepository.completeByIdAndLeaseToken(
            taskSubmissionContext.taskSubmissionId(),
            taskSubmissionContext.leaseToken(),
            passedWeight,
            totalWeight,
            passedCount,
            totalCount,
            scorePercentage,
            Instant.now()
        );

        logLeaseTokenMismatch(taskSubmissionContext.taskSubmissionId(), updatedRows);

        if (updatedRows == 0) {
            return;
        }

        taskSubmissionResultRepository.deleteByTaskSubmissionId(taskSubmissionContext.taskSubmissionId());

        TaskSubmission taskSubmissionProxy =
            taskSubmissionRepository.getReferenceById(taskSubmissionContext.taskSubmissionId());

        List<TaskSubmissionResult> taskSubmissionResults = outcomes.stream()
            .map(outcome -> TaskSubmissionResult.builder()
                .submission(taskSubmissionProxy)
                .testCase(taskTestCaseRepository.getReferenceById(outcome.testCaseId()))
                .status(outcome.status())
                .stdout(outcome.stdout())
                .stderr(outcome.stderr())
                .exitCode(outcome.exitCode())
                .executionTimeMs(outcome.executionTimeMs())
                .build())
            .toList();

        taskSubmissionResultRepository.saveAll(taskSubmissionResults);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(Long taskSubmissionId, UUID leaseToken) {
        int updatedRows = taskSubmissionRepository.markFailedByIdAndLeaseToken(
            taskSubmissionId,
            leaseToken,
            PROCESSING_ERROR_MESSAGE,
            Instant.now()
        );

        logLeaseTokenMismatch(taskSubmissionId, updatedRows);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markGraded(Long taskSubmissionId) {
        taskSubmissionRepository.markGradedById(taskSubmissionId, Instant.now());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void requeueExpired() {
        Instant now = Instant.now();
        int maxProcessingAttempts = taskSubmissionProperties.maxProcessingAttempts();

        int failedRows = taskSubmissionRepository.failExpiredWithExhaustedAttempts(
            TaskSubmissionStatus.RUNNING,
            now,
            maxProcessingAttempts,
            MAX_PROCESSING_ATTEMPTS_ERROR_MESSAGE
        );

        int requeuedRows = taskSubmissionRepository.requeueExpiredWithRemainingAttempts(
            TaskSubmissionStatus.RUNNING,
            now,
            maxProcessingAttempts
        );

        if (failedRows > 0 || requeuedRows > 0) {
            log.warn("Reaper requeued {} and failed {} expired task submissions", requeuedRows, failedRows);
        }
    }

    @Transactional(readOnly = true)
    public List<Long> findUnfinishedGradingTaskSubmissionIds(Instant threshold) {
        return taskSubmissionRepository.findUnfinishedGradingTaskSubmissionIds(
            TaskSubmissionStatus.COMPLETED,
            threshold
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void releaseClaim(Long taskSubmissionId, UUID leaseToken) {
        int updatedRows = taskSubmissionRepository.releaseClaimByIdAndLeaseToken(
            taskSubmissionId,
            leaseToken,
            Instant.now()
        );

        logLeaseTokenMismatch(taskSubmissionId, updatedRows);
    }

    private void logLeaseTokenMismatch(Long taskSubmissionId, int updatedRows) {
        if (updatedRows == 0) {
            log.warn("Could not update task submission {} due to lease token mismatch", taskSubmissionId);
        }
    }
}