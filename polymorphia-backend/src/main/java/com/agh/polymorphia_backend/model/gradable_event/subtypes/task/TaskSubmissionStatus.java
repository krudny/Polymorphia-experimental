package com.agh.polymorphia_backend.model.gradable_event.subtypes.task;

public enum TaskSubmissionStatus {
    QUEUED, RUNNING, COMPLETED, COMPILE_ERROR, RUNTIME_ERROR, TIMEOUT, INTERNAL_ERROR
}