package com.agh.polymorphia_backend.dto.response.task;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskTestCaseDto {
    private String name;
    private Integer orderIndex;
    private String input;
    private String expectedOutput;
}
