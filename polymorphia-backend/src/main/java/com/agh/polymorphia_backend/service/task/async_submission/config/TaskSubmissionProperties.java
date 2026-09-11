package com.agh.polymorphia_backend.service.task.async_submission.config;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "task.submission")
public record TaskSubmissionProperties(
    @Min(1) int batchSize,
    @Min(1) int workerThreads,
    @Min(1) int leaseDurationSeconds,
    @Min(1) int maxProcessingAttempts,
    @Min(1) int gradingStaleAfterSeconds
) {
  public Duration gradingStaleAfter() {
    return Duration.ofSeconds(gradingStaleAfterSeconds);
  }
}