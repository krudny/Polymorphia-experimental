package com.agh.polymorphia_backend.dto.request.course_import.criterion;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CriterionDetailsRequestDto {
    @NotEmpty
    private String key;

    @NotEmpty
    private String name;

    @NotNull
    private BigDecimal maxXp;

    @NotNull
    @Builder.Default
    private List<CriterionRewardDetailsRequestDto> rewards = new ArrayList<>();
}
