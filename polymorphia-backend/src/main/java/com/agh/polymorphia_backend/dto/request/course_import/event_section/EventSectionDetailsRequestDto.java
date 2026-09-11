package com.agh.polymorphia_backend.dto.request.course_import.event_section;

import com.agh.polymorphia_backend.dto.request.course_import.gradable_event.GradableEventDetailsRequestDto;
import com.agh.polymorphia_backend.model.event_section.EventSectionType;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = AssignmentSectionDetailsRequestDto.class, name = "assignment"),
        @JsonSubTypes.Type(value = TestSectionDetailsRequestDto.class, name = "test"),
        @JsonSubTypes.Type(value = ProjectSectionDetailsRequestDto.class, name = "project"),
        @JsonSubTypes.Type(value = TaskSectionDetailsRequestDto.class, name = "task")
})
@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public abstract class EventSectionDetailsRequestDto {
    @NotEmpty
    private String key;

    @NotNull
    private EventSectionType type;

    @NotEmpty
    private String name;

    @NotNull
    private Boolean isShownInRoadmap;

    @NotNull
    private Boolean eventsWithTopics;

    @NotNull
    private Boolean isHidden;


    public abstract List<? extends GradableEventDetailsRequestDto> getGradableEvents();
}
