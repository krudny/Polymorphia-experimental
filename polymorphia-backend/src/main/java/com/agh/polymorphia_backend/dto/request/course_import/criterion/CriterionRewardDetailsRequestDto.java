package com.agh.polymorphia_backend.dto.request.course_import.criterion;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CriterionRewardDetailsRequestDto {
    @NotEmpty
    private String rewardKey;

    @NotNull
    private Integer maxAmount;
}
