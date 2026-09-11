package com.agh.polymorphia_backend.dto.request.course_import.variant;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VariantDetailsRequestDto {
    @NotEmpty
    private String key;

    @NotEmpty
    private String name;

    @NotEmpty
    private String shortCode;

    @NotEmpty
    private String description;

    @NotEmpty
    private String imageUrl;
}
