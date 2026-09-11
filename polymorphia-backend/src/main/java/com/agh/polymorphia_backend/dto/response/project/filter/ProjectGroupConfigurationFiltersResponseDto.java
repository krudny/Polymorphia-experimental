package com.agh.polymorphia_backend.dto.response.project.filter;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ProjectGroupConfigurationFiltersResponseDto {
    @NotNull
    private List<FilterOptionResponseDto> options;

    @NotNull
    private Long max;

    @NotNull
    private List<String> defaultValues;

    @NotNull
    private String additionalInfo;
}
