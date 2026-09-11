package com.agh.polymorphia_backend.service.task;

import com.agh.polymorphia_backend.dto.response.task.RemoteExecutionResponseDto;
import com.agh.polymorphia_backend.model.gradable_event.subtypes.task.TaskOutputMatchMode;
import com.agh.polymorphia_backend.model.gradable_event.subtypes.task.TaskTestCaseStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TaskTestCaseEvaluator {

    private final TaskOutputMatcher taskOutputMatcher;

    public TaskTestCaseStatus evaluate(
            TaskOutputMatchMode outputMatchMode,
            String expectedOutput,
            RemoteExecutionResponseDto executionResponse
    ) {
        if (Boolean.TRUE.equals(executionResponse.getTimedOut())) {
            return TaskTestCaseStatus.TIMEOUT;
        }
        if (executionResponse.getExitCode() == null || executionResponse.getExitCode() != 0) {
            return TaskTestCaseStatus.RUNTIME_ERROR;
        }

        boolean matched = taskOutputMatcher.matches(outputMatchMode, expectedOutput, executionResponse.getStdout());
        return matched ? TaskTestCaseStatus.PASSED : TaskTestCaseStatus.FAILED;
    }
}
