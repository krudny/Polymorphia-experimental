package com.agh.polymorphia_backend.repository.reward.projection;

import com.agh.polymorphia_backend.model.reward.item.FlatBonusItemBehavior;
import com.agh.polymorphia_backend.model.reward.item.ItemType;

import java.math.BigDecimal;

public interface ItemDetailsDetailsProjection {
    Long getId();

    String getKey();

    String getName();

    String getDescription();

    String getImageUrl();

    Integer getLimit();

    Long getOrderIndex();

    String getEventSectionKey();

    ItemType getItemType();

    BigDecimal getXpBonus();

    FlatBonusItemBehavior getBehavior();

    Integer getPercentageBonus();

}
