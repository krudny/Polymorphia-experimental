package com.agh.polymorphia_backend.dto.request.course_import.gradable_event;

import com.agh.polymorphia_backend.dto.request.course_import.criterion.CriterionDetailsRequestDto;
import com.agh.polymorphia_backend.model.event_section.EventSectionType;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = AssignmentDetailsRequestDto.class, name = "assignment"),
        @JsonSubTypes.Type(value = TestDetailsRequestDto.class, name = "test"),
        @JsonSubTypes.Type(value = ProjectDetailsRequestDto.class, name = "project"),
        @JsonSubTypes.Type(value = TaskDetailsRequestDto.class, name = "task")
})
@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class GradableEventDetailsRequestDto {
    @NotNull
    private EventSectionType type;

    @NotEmpty
    private String key;

    @NotEmpty
    private String name;

    private String topic;

    private String markdownSourceUrl;

    @NotNull
    private Boolean isHidden;

    @NotNull
    private Boolean isLocked;

    @NotNull
    @Builder.Default
    private List<CriterionDetailsRequestDto> criteria = new ArrayList<>();
}
