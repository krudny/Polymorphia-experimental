package com.agh.polymorphia_backend.dto.response.task;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ExecuteTaskResponseDto {
    private final List<TestCaseResultDto> results;
}
