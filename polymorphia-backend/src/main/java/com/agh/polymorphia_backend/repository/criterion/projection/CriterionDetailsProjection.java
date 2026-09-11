package com.agh.polymorphia_backend.repository.criterion.projection;

import java.math.BigDecimal;

public interface CriterionDetailsProjection {
    Long getId();

    String getName();

    BigDecimal getMaxXp();

    Long getGradableEventId();

    String getKey();
}
