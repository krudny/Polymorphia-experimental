package com.agh.polymorphia_backend.service.task.async_submission;

import com.agh.polymorphia_backend.dto.request.task.RemoteExecutionRequestDto;
import com.agh.polymorphia_backend.dto.response.task.RemoteExecutionResponseDto;
import com.agh.polymorphia_backend.model.gradable_event.subtypes.task.TaskTestCaseStatus;
import com.agh.polymorphia_backend.service.task.TaskOutputTruncator;
import com.agh.polymorphia_backend.service.task.TaskTestCaseEvaluator;
import com.agh.polymorphia_backend.service.task.dto.TaskSubmissionContext;
import com.agh.polymorphia_backend.service.task.dto.TaskTestCaseOutcome;
import com.agh.polymorphia_backend.service.task.dto.TaskTestCaseSpec;
import com.agh.polymorphia_backend.service.task.remote_client.CodeExecutorClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskSubmissionExecutionService {

    private final CodeExecutorClient codeExecutorClient;
    private final TaskTestCaseEvaluator taskTestCaseEvaluator;
    private final TaskOutputTruncator taskOutputTruncator;

    public List<TaskTestCaseOutcome> run(TaskSubmissionContext taskSubmissionContext) {
        List<TaskTestCaseOutcome> outcomes = new ArrayList<>();

        for (TaskTestCaseSpec testCaseSpec : taskSubmissionContext.testCases()) {
            RemoteExecutionRequestDto remoteRequest = RemoteExecutionRequestDto
                    .from(testCaseSpec, taskSubmissionContext.language(), taskSubmissionContext.sourceCode());

            RemoteExecutionResponseDto executionResponse = codeExecutorClient.executeSync(remoteRequest);

            TaskTestCaseStatus status = taskTestCaseEvaluator.evaluate(
                    taskSubmissionContext.outputMatchMode(),
                    testCaseSpec.expectedOutput(),
                    executionResponse
            );

            String truncatedStdout = taskOutputTruncator.truncate(executionResponse.getStdout());
            String truncatedStderr = taskOutputTruncator.truncate(executionResponse.getStderr());

            TaskTestCaseOutcome outcome = TaskTestCaseOutcome.from(
                    testCaseSpec,
                    status,
                    truncatedStdout,
                    truncatedStderr,
                    executionResponse
            );

            outcomes.add(outcome);
        }

        return outcomes;
    }
}
