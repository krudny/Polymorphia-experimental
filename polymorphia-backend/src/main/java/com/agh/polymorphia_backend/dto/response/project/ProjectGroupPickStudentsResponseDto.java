package com.agh.polymorphia_backend.dto.response.project;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ProjectGroupPickStudentsResponseDto {
    @NotNull
    private Long id;

    @NotNull
    private String fullName;

    @NotNull
    private String animalName;

    @NotNull
    private String evolutionStage;

    @NotNull
    private String group;

    @NotNull
    private String imageUrl;
    private int position;
}
