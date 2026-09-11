package com.agh.polymorphia_backend.dto.response.user_context;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
@AllArgsConstructor
public class BaseUserDetailsResponseDto {
    @NotNull
    private Long id;

    @NotEmpty
    private String fullName;

    @NotNull
    private Long courseId;

    @NotNull
    private String imageUrl;
}
