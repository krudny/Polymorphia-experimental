package com.agh.polymorphia_backend.service.task.dto;

import com.agh.polymorphia_backend.model.gradable_event.subtypes.task.TaskGradingStrategy;
import com.agh.polymorphia_backend.model.gradable_event.subtypes.task.TaskOutputMatchMode;
import com.agh.polymorphia_backend.model.gradable_event.subtypes.task.TaskSubmission;
import com.agh.polymorphia_backend.model.gradable_event.subtypes.task.TaskSupportedLanguage;
import com.agh.polymorphia_backend.model.gradable_event.subtypes.task.TaskTestCase;

import java.util.List;
import java.util.UUID;

public record TaskSubmissionContext(
    Long taskSubmissionId,
    Long taskId,
    Long animalId,
    TaskSupportedLanguage language,
    String sourceCode,
    TaskOutputMatchMode outputMatchMode,
    TaskGradingStrategy gradingStrategy,
    List<TaskTestCaseSpec> testCases,
    UUID leaseToken
) {

    public static TaskSubmissionContext from(TaskSubmission taskSubmission, List<TaskTestCase> testCases) {
        List<TaskTestCaseSpec> testCaseSpecs = testCases.stream()
                .map(testCase -> TaskTestCaseSpec.from(taskSubmission.getTask(), testCase))
                .toList();

        return new TaskSubmissionContext(
                taskSubmission.getId(),
                taskSubmission.getTask().getId(),
                taskSubmission.getAnimal().getId(),
                taskSubmission.getLanguage(),
                taskSubmission.getSourceCode(),
                taskSubmission.getTask().getOutputMatchMode(),
                taskSubmission.getTask().getGradingStrategy(),
                testCaseSpecs,
                taskSubmission.getLeaseToken()
        );
    }
}
