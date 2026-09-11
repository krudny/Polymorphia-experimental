package com.agh.polymorphia_backend.service.task;

import com.agh.polymorphia_backend.model.gradable_event.subtypes.task.TaskOutputMatchMode;
import org.springframework.stereotype.Component;


@Component
public class TaskOutputMatcher {

    public boolean matches(TaskOutputMatchMode outputMatchMode, String expected, String actual) {
        if (expected == null || actual == null) {
            return false;
        }

        String normalizedExpected = expected.replace("\r\n", "\n").strip();
        String normalizedActual = actual.replace("\r\n", "\n").strip();

        return switch (outputMatchMode) {
            case EXACT -> normalizedExpected.equals(normalizedActual);
        };
    }
}