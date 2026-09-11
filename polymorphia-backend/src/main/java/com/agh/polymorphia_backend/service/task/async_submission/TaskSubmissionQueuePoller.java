package com.agh.polymorphia_backend.service.task.async_submission;

import com.agh.polymorphia_backend.service.task.async_submission.config.TaskSubmissionProperties;
import com.agh.polymorphia_backend.service.task.dto.TaskSubmissionContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskSubmissionQueuePoller {

    private final TaskSubmissionPersistenceService taskSubmissionPersistenceService;
    private final TaskSubmissionProcessor taskSubmissionProcessor;
    private final ThreadPoolTaskExecutor taskSubmissionExecutor;
    private final TaskSubmissionProperties taskSubmissionProperties;

    @Scheduled(fixedDelayString = "${task.submission.poll-interval-ms:1000}")
    public void pollQueue() {
        int availableSlots = availableSlots();

        if (availableSlots <= 0) {
            return;
        }

        int batchSize = Math.min(taskSubmissionProperties.batchSize(), availableSlots);

        List<TaskSubmissionContext> claimedTaskSubmissions =
            taskSubmissionPersistenceService.claimNextTaskSubmissions(batchSize);

        for (TaskSubmissionContext taskSubmissionContext : claimedTaskSubmissions) {
            submitForProcessing(taskSubmissionContext);
        }
    }

    private void submitForProcessing(TaskSubmissionContext taskSubmissionContext) {
        try {
            taskSubmissionExecutor.execute(() -> taskSubmissionProcessor.process(taskSubmissionContext));
        } catch (RejectedExecutionException exception) {
            log.warn("Task submission {} rejected by executor, releasing claim",
                taskSubmissionContext.taskSubmissionId());
            taskSubmissionPersistenceService.releaseClaim(
                taskSubmissionContext.taskSubmissionId(),
                taskSubmissionContext.leaseToken()
            );
        }
    }

    private int availableSlots() {
        ThreadPoolExecutor threadPoolExecutor = taskSubmissionExecutor.getThreadPoolExecutor();
        return taskSubmissionExecutor.getMaxPoolSize()
            - threadPoolExecutor.getActiveCount()
            - threadPoolExecutor.getQueue().size();
    }
}