package com.agh.polymorphia_backend.service.task.dto;

import com.agh.polymorphia_backend.model.gradable_event.subtypes.task.Task;
import com.agh.polymorphia_backend.model.gradable_event.subtypes.task.TaskTestCase;

public record TaskLimits(
    Integer cpuTimeLimitMs,
    Integer wallTimeLimitMs,
    Integer memoryLimitMb
) {

    public static TaskLimits from(Task task, TaskTestCase taskTestCase) {
        Integer cpuTimeLimitMs = taskTestCase.getTimeLimitOverrideMs() != null
            ? taskTestCase.getTimeLimitOverrideMs()
            : task.getCpuTimeLimitMs();

        return new TaskLimits(
            cpuTimeLimitMs,
            task.getWallTimeLimitMs(),
            task.getMemoryLimitMb()
        );
    }
}
