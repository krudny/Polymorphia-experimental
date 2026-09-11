package com.agh.polymorphia_backend.repository.criterion.projection;

public interface CriterionRewardDetailsProjection {
    Long getCriterionId();

    String getRewardKey();

    Integer getMaxAmount();
}
