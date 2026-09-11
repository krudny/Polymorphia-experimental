package com.agh.polymorphia_backend.dto.response.task;

import com.agh.polymorphia_backend.model.gradable_event.subtypes.task.TaskSubmissionStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Builder
public class TaskSubmissionStatusResponseDto {
    private final Long submissionId;

    private final TaskSubmissionStatus status;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final BigDecimal score;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final Integer passedCount;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final Integer totalCount;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final Integer totalExecutionTimeMs;

    private final Instant createdDate;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final List<TestCaseResultDto> visibleResults;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String errorMessage;
}