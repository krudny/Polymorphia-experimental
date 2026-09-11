package com.agh.polymorphia_backend.repository.project.projection;

public interface ProjectVariantDetailsProjection {
    Long getVariantCategoryId();

    String getKey();

    String getName();

    String getDescription();

    String getImageUrl();

    String getShortCode();
}
