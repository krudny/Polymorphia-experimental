package com.agh.polymorphia_backend.dto.response.task;

import com.agh.polymorphia_backend.model.gradable_event.subtypes.task.TaskSubmissionStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SubmitTaskResponseDto {
    private final Long submissionId;

    private final TaskSubmissionStatus status;
}
