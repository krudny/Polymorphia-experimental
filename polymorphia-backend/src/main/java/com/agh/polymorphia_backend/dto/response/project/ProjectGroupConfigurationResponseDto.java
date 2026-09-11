package com.agh.polymorphia_backend.dto.response.project;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Builder
@Getter
public class ProjectGroupConfigurationResponseDto {
    @NotNull
    private List<Long> studentIds;

    @NotNull
    private Map<Long, Long> selectedVariants;
}
