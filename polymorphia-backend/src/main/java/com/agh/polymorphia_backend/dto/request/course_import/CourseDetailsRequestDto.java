package com.agh.polymorphia_backend.dto.request.course_import;

import com.agh.polymorphia_backend.dto.request.course_import.event_section.EventSectionDetailsRequestDto;
import com.agh.polymorphia_backend.dto.request.course_import.reward.ChestDetailsRequestDto;
import com.agh.polymorphia_backend.dto.request.course_import.reward.ItemDetailsRequestDto;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
@NoArgsConstructor
public class CourseDetailsRequestDto {
    @NotEmpty
    @EqualsAndHashCode.Include
    private String name;

    @EqualsAndHashCode.Include
    private String markdownSourceUrl;

    @NotEmpty
    @EqualsAndHashCode.Include
    private String imageUrl;

    @NotEmpty
    @EqualsAndHashCode.Include
    private String coordinatorImageUrl;

    @NotEmpty
    @EqualsAndHashCode.Include
    private String instructorImageUrl;

    @NotNull
    @Builder.Default
    private List<EvolutionStageDetailsRequestDto> evolutionStages = new ArrayList<>();

    @NotNull
    @Builder.Default
    private List<EventSectionDetailsRequestDto> eventSections = new ArrayList<>();

    @NotNull
    @Builder.Default
    private List<ItemDetailsRequestDto> items = new ArrayList<>();

    @NotNull
    @Builder.Default
    private List<ChestDetailsRequestDto> chests = new ArrayList<>();

    @NotNull
    @Builder.Default
    @JsonSetter(contentNulls = Nulls.SKIP)
    private List<String> roadmapOrderKeys = new ArrayList<>();
}
