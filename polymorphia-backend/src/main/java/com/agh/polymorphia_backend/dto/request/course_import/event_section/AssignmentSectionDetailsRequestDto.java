package com.agh.polymorphia_backend.dto.request.course_import.event_section;

import com.agh.polymorphia_backend.dto.request.course_import.gradable_event.AssignmentDetailsRequestDto;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Getter
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentSectionDetailsRequestDto extends EventSectionDetailsRequestDto {
    @NotNull
    @Builder.Default
    private List<AssignmentDetailsRequestDto> gradableEvents = new ArrayList<>();

}
