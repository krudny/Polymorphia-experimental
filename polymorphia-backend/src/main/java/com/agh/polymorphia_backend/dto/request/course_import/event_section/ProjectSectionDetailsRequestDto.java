package com.agh.polymorphia_backend.dto.request.course_import.event_section;

import com.agh.polymorphia_backend.dto.request.course_import.gradable_event.ProjectDetailsRequestDto;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Getter
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class ProjectSectionDetailsRequestDto extends EventSectionDetailsRequestDto {
    @NotNull
    @Builder.Default
    private List<ProjectDetailsRequestDto> gradableEvents = new ArrayList<>();
}
