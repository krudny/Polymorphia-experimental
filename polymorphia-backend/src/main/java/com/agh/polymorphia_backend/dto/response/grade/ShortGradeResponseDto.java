package com.agh.polymorphia_backend.dto.response.grade;

import com.agh.polymorphia_backend.dto.response.criteria.CriterionGradeResponseDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@SuperBuilder
@Getter
@EqualsAndHashCode
public class ShortGradeResponseDto {
    @NotNull
    private Boolean isGraded;

    @NotNull
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String comment;

    private Boolean hasReward;

    @NotNull
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Builder.Default
    private List<CriterionGradeResponseDto> criteria = new ArrayList<>();
}
