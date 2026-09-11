package com.agh.polymorphia_backend.repository.reward.projection;

import com.agh.polymorphia_backend.model.reward.chest.ChestBehavior;

import java.util.List;

public interface ChestDetailsDetailsProjection {
    String getKey();

    String getName();

    String getDescription();

    String getImageUrl();

    ChestBehavior getBehavior();

    Long getOrderIndex();

    List<String> getItemKeys();

}
