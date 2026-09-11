package com.agh.polymorphia_backend.dto.response.project;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class ProjectVariantWithCategoryNameResponseDto extends ProjectVariantResponseDto{
    @NotEmpty
    private String categoryName;
}
