package com.agh.polymorphia_backend.service.task.dto;

import com.agh.polymorphia_backend.model.gradable_event.subtypes.task.Task;
import com.agh.polymorphia_backend.model.gradable_event.subtypes.task.TaskTestCase;

import java.math.BigDecimal;

public record TaskTestCaseSpec(
    Long testCaseId,
    String input,
    String expectedOutput,
    BigDecimal weight,
    TaskLimits limits
) {

    public static TaskTestCaseSpec from(Task task, TaskTestCase taskTestCase) {
        return new TaskTestCaseSpec(
            taskTestCase.getId(),
            taskTestCase.getInput(),
            taskTestCase.getExpectedOutput(),
            taskTestCase.getWeight() != null ? taskTestCase.getWeight() : BigDecimal.ONE,
            TaskLimits.from(task, taskTestCase)
        );
    }
}
