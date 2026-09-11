package com.agh.polymorphia_backend.dto.response.task;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TestCaseResultDto {
    private final Long testCaseId;

    private final Integer orderIndex;

    private final String name;

    private final String input;

    private final String expectedOutput;

    private final String actualOutput;

    private final Boolean passed;

    private final String stderr;

    private final Integer exitCode;

    private final Integer executionTimeMs;
}
