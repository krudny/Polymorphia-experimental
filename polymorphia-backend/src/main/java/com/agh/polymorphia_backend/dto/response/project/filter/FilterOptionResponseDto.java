package com.agh.polymorphia_backend.dto.response.project.filter;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FilterOptionResponseDto {
    @NotNull
    private String value;

    private String label;
    private String specialBehavior;
}
