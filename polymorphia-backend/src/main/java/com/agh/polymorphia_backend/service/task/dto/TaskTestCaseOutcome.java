package com.agh.polymorphia_backend.service.task.dto;

import com.agh.polymorphia_backend.dto.response.task.RemoteExecutionResponseDto;
import com.agh.polymorphia_backend.model.gradable_event.subtypes.task.TaskTestCaseStatus;

import java.math.BigDecimal;

public record TaskTestCaseOutcome(
    Long testCaseId,
    TaskTestCaseStatus status,
    String stdout,
    String stderr,
    Integer exitCode,
    Integer executionTimeMs,
    BigDecimal weight
) {

    public static TaskTestCaseOutcome from(
            Long testCaseId,
            TaskTestCaseStatus status,
            String stdout,
            String stderr,
            Integer exitCode,
            Integer executionTimeMs,
            BigDecimal weight
    ) {
        return new TaskTestCaseOutcome(
                testCaseId,
                status,
                stdout,
                stderr,
                exitCode,
                executionTimeMs,
                weight
        );
    }

    public static TaskTestCaseOutcome from(
            TaskTestCaseSpec testCaseSpec,
            TaskTestCaseStatus status,
            String stdout,
            String stderr,
            RemoteExecutionResponseDto executionResponse
    ) {
        return new TaskTestCaseOutcome(
                testCaseSpec.testCaseId(),
                status,
                stdout,
                stderr,
                executionResponse.getExitCode(),
                executionResponse.getDurationMs(),
                testCaseSpec.weight()
        );
    }
}
