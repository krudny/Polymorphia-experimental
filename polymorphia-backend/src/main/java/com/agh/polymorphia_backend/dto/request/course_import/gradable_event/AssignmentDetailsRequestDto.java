package com.agh.polymorphia_backend.dto.request.course_import.gradable_event;

import com.agh.polymorphia_backend.dto.request.course_import.SubmissionRequirementDetailsRequestDto;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Getter
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class AssignmentDetailsRequestDto extends GradableEventDetailsRequestDto {
    @NotNull
    @Builder.Default
    private List<SubmissionRequirementDetailsRequestDto> submissionRequirements = new ArrayList<>();
}
