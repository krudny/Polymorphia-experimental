package com.agh.polymorphia_backend.model.reward;

import com.agh.polymorphia_backend.model.reward.item.ItemType;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "percentage_bonus_items")
@PrimaryKeyJoinColumn(name = "item_id")

@Getter
@Setter
@ToString(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public class PercentageBonusItem extends Item {
    @NotNull
    private Integer percentageBonus;

    @Override
    public ItemType getItemType() {
        return ItemType.PERCENTAGE_BONUS;
    }
}
