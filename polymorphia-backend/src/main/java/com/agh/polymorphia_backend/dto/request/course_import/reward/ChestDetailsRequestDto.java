package com.agh.polymorphia_backend.dto.request.course_import.reward;

import com.agh.polymorphia_backend.model.reward.chest.ChestBehavior;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Builder
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class ChestDetailsRequestDto {
    @NotEmpty
    private String key;

    @NotEmpty
    private String name;

    @NotEmpty
    private String description;

    @NotEmpty
    private String imageUrl;

    @NotNull
    private ChestBehavior behavior;

    @NotNull
    @JsonSetter(contentNulls = Nulls.SKIP)
    private List<String> itemKeys;
}
