package com.agh.polymorphia_backend.dto.request.course_import.reward;

import com.agh.polymorphia_backend.model.reward.item.FlatBonusItemBehavior;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Getter
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class FlatBonusItemDetailsRequestDto extends ItemDetailsRequestDto {
    @NotNull
    private BigDecimal xpBonus;

    @NotNull
    private FlatBonusItemBehavior behavior;

}
