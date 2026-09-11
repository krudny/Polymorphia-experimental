package com.agh.polymorphia_backend.dto.request.course_import.reward;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class PercentageBonusItemDetailsRequestDto extends ItemDetailsRequestDto {
    @NotNull
    private Integer percentageBonus;
}
