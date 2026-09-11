package com.agh.polymorphia_backend.dto.request.project;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public class ProjectGroupUpdateRequestDto {
    @NotNull
    List<Long> studentIds;

    @NotNull
    Map<Long, Long> selectedVariants;
}
