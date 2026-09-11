package com.agh.polymorphia_backend.dto.response.project;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class ProjectCategoryWithVariantsResponseDto {
    @NotNull
    private Long id;

    @NotNull
    private String name;

    @NotNull
    private List<ProjectVariantResponseDto> variants;
}
