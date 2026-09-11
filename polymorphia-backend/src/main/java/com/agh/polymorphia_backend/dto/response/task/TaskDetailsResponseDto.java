package com.agh.polymorphia_backend.dto.response.task;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDetailsResponseDto {
    private List<TaskAllowedLanguageDto> allowedLanguages;

    private List<TaskTestCaseDto> testCases;
}
