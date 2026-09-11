package com.agh.polymorphia_backend.dto.request.course_import.reward;

import com.agh.polymorphia_backend.model.reward.item.ItemType;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = FlatBonusItemDetailsRequestDto.class, name = "FLAT_BONUS"),
        @JsonSubTypes.Type(value = PercentageBonusItemDetailsRequestDto.class, name = "PERCENTAGE_BONUS")
})
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode()
public class ItemDetailsRequestDto {
    @NotEmpty
    private String key;

    @NotEmpty
    private String name;

    @NotEmpty
    private String description;

    @NotEmpty
    private String imageUrl;

    @NotNull
    private Integer limit;

    @NotEmpty
    private String eventSectionKey;

    @NotNull
    private ItemType type;

}
