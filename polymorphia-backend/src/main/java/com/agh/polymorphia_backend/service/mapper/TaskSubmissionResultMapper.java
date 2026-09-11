package com.agh.polymorphia_backend.service.mapper;

import com.agh.polymorphia_backend.dto.response.task.TestCaseResultDto;
import com.agh.polymorphia_backend.model.gradable_event.subtypes.task.TaskSubmissionResult;
import com.agh.polymorphia_backend.model.gradable_event.subtypes.task.TaskTestCase;
import com.agh.polymorphia_backend.model.gradable_event.subtypes.task.TaskTestCaseStatus;
import org.springframework.stereotype.Component;

@Component
public class TaskSubmissionResultMapper {

    public TestCaseResultDto toDto(TaskSubmissionResult result) {
        TaskTestCase testCase = result.getTestCase();

        return toDto(
            testCase,
            result.getStatus(),
            result.getStdout(),
            result.getStderr(),
            result.getExitCode(),
            result.getExecutionTimeMs()
        );
    }

    public TestCaseResultDto toDto(
        TaskTestCase testCase,
        TaskTestCaseStatus status,
        String stdout,
        String stderr,
        Integer exitCode,
        Integer executionTimeMs
    ) {
        return TestCaseResultDto.builder()
            .testCaseId(testCase.getId())
            .orderIndex(testCase.getOrderIndex())
            .name(testCase.getName())
            .input(testCase.getInput())
            .expectedOutput(testCase.getExpectedOutput())
            .actualOutput(stdout)
            .passed(status == TaskTestCaseStatus.PASSED)
            .stderr(stderr)
            .exitCode(exitCode)
            .executionTimeMs(executionTimeMs)
            .build();
    }
}
