package com.agh.polymorphia_backend.service.task;

import com.agh.polymorphia_backend.dto.request.task.ExecuteTaskRequestDto;
import com.agh.polymorphia_backend.dto.request.task.RemoteExecutionRequestDto;
import com.agh.polymorphia_backend.dto.response.task.ExecuteTaskResponseDto;
import com.agh.polymorphia_backend.dto.response.task.RemoteExecutionResponseDto;
import com.agh.polymorphia_backend.dto.response.task.TestCaseResultDto;
import com.agh.polymorphia_backend.model.gradable_event.subtypes.task.Task;
import com.agh.polymorphia_backend.model.gradable_event.subtypes.task.TaskTestCase;
import com.agh.polymorphia_backend.model.gradable_event.subtypes.task.TaskTestCaseStatus;
import com.agh.polymorphia_backend.repository.task.TaskTestCaseRepository;
import com.agh.polymorphia_backend.service.mapper.TaskSubmissionResultMapper;
import com.agh.polymorphia_backend.service.task.dto.TaskTestCaseSpec;
import com.agh.polymorphia_backend.service.task.remote_client.CodeExecutorClient;
import com.agh.polymorphia_backend.service.validation.TaskAuthorizer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskRunService {

    private final TaskService taskService;
    private final TaskTestCaseRepository taskTestCaseRepository;
    private final CodeExecutorClient codeExecutorClient;
    private final TaskTestCaseEvaluator taskTestCaseEvaluator;
    private final TaskOutputTruncator taskOutputTruncator;
    private final TaskSubmissionResultMapper taskSubmissionResultMapper;
    private final TaskAuthorizer taskAuthorizer;

    public ExecuteTaskResponseDto runTask(Long taskId, ExecuteTaskRequestDto request) {
        taskAuthorizer.checkTaskAccess(taskId);
        Task task = taskService.getTask(taskId);
        taskService.validateTaskLanguage(taskId, request.getTaskLanguage());

        List<TaskTestCase> visibleTestCases = taskTestCaseRepository.findVisibleByTaskId(taskId);
        List<TestCaseResultDto> results = visibleTestCases.stream()
                .map(testCase -> runSingleTestCase(task, request, testCase))
                .toList();

        return ExecuteTaskResponseDto.builder()
                .results(results)
                .build();
    }

    private TestCaseResultDto runSingleTestCase(Task task, ExecuteTaskRequestDto request, TaskTestCase testCase) {
        TaskTestCaseSpec testCaseSpec = TaskTestCaseSpec.from(task, testCase);

        RemoteExecutionRequestDto remoteRequest = RemoteExecutionRequestDto
                .from(testCaseSpec, request.getTaskLanguage(), request.getSourceCode());

        RemoteExecutionResponseDto executionResponse;
        try {
            executionResponse = codeExecutorClient.executeSync(remoteRequest);
        } catch (Exception exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Usługa wykonywania kodu jest chwilowo niedostępna."
            );
        }

        TaskTestCaseStatus status = taskTestCaseEvaluator.evaluate(
                task.getOutputMatchMode(),
                testCase.getExpectedOutput(),
                executionResponse
        );

        String truncatedStdout = taskOutputTruncator.truncate(executionResponse.getStdout());
        String truncatedStderr = taskOutputTruncator.truncate(executionResponse.getStderr());

        return taskSubmissionResultMapper.toDto(
                testCase,
                status,
                truncatedStdout,
                truncatedStderr,
                executionResponse.getExitCode(),
                executionResponse.getDurationMs()
        );
    }
}
