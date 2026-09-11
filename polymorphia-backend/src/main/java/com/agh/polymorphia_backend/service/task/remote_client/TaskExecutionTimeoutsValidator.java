package com.agh.polymorphia_backend.service.task.remote_client;

import com.agh.polymorphia_backend.service.task.async_submission.config.TaskSubmissionProperties;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TaskExecutionTimeoutsValidator
{
    private final int readTimeoutMs;
    private final int maxWallTimeLimitMs;
    private final int leaseDurationMs;

    public TaskExecutionTimeoutsValidator(
        @Value("${code-executor.read-timeout-seconds}") int readTimeoutSeconds,
        @Value("${code-executor.max-wall-time-limit-ms}") int maxWallTimeLimitMs,
        TaskSubmissionProperties taskSubmissionProperties
    ) {
        this.readTimeoutMs = readTimeoutSeconds * 1000;
        this.maxWallTimeLimitMs = maxWallTimeLimitMs;
        this.leaseDurationMs = taskSubmissionProperties.leaseDurationSeconds() * 1000;
    }

    @PostConstruct
    public void validate() {
        if (readTimeoutMs <= maxWallTimeLimitMs) {
            throw new IllegalStateException(
                String.format(
                    "Read executor timeout (%dms) must exceed wall time limit (%dms), "
                        + "otherwise the client aborts before the executor can finish a request running at its limit.",
                    readTimeoutMs, maxWallTimeLimitMs
                )
            );
        }

        if (leaseDurationMs <= readTimeoutMs) {
            throw new IllegalStateException(
                String.format(
                    "Lease duration (%dms) must exceed read executor timeout (%dms), "
                        + "otherwise a lease can expire mid-request and be reclaimed by another worker.",
                    leaseDurationMs, readTimeoutMs
                )
            );
        }
    }
}
