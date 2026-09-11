package com.agh.polymorphia_backend.dto.request.course_import.gradable_event;

import com.agh.polymorphia_backend.dto.request.course_import.SubmissionRequirementDetailsRequestDto;
import com.agh.polymorphia_backend.dto.request.course_import.variant.VariantCategoryDetailsRequestDto;
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
public class ProjectDetailsRequestDto extends GradableEventDetailsRequestDto {
    @NotNull
    private Boolean allowCrossCourseGroupProjectGroup;

    @NotNull
    @Builder.Default
    private List<VariantCategoryDetailsRequestDto> variantCategories = new ArrayList<>();

    @NotNull
    @Builder.Default
    private List<SubmissionRequirementDetailsRequestDto> submissionRequirements = new ArrayList<>();
}
