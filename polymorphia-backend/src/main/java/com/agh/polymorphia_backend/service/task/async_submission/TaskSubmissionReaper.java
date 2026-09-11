package com.agh.polymorphia_backend.service.task.async_submission;

import com.agh.polymorphia_backend.service.task.async_submission.config.TaskSubmissionProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskSubmissionReaper {

    private final TaskSubmissionPersistenceService taskSubmissionPersistenceService;
    private final TaskSubmissionProcessor taskSubmissionProcessor;
    private final TaskSubmissionProperties taskSubmissionProperties;

    @Scheduled(fixedDelayString = "${task.submission.lease-reaper-interval-ms:1000}")
    public void requeueExpiredLeases() {
        taskSubmissionPersistenceService.requeueExpired();
    }

    @Scheduled(fixedDelayString = "${task.submission.grading-reaper-interval-ms:2000}")
    public void retryUnfinishedGrading() {
        Instant threshold = Instant.now().minus(taskSubmissionProperties.gradingStaleAfter());

        taskSubmissionPersistenceService
                .findUnfinishedGradingTaskSubmissionIds(threshold)
                .forEach(taskSubmissionProcessor::grade);
    }
}