package com.agh.polymorphia_backend.model.reward;


import com.agh.polymorphia_backend.model.reward.item.FlatBonusItemBehavior;
import com.agh.polymorphia_backend.model.reward.item.ItemType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Table(name = "flat_bonus_items")
@PrimaryKeyJoinColumn(name = "item_id")
@Getter
@Setter
@ToString(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public class FlatBonusItem extends Item {
    @NotNull
    @PositiveOrZero
    @Column(precision = 4, scale = 1)
    private BigDecimal xpBonus;

    @NotNull
    @Enumerated(EnumType.STRING)
    private FlatBonusItemBehavior behavior;

    @Override
    public ItemType getItemType() {
        return ItemType.FLAT_BONUS;
    }
}
