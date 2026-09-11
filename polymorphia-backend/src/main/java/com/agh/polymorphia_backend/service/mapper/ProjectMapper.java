package com.agh.polymorphia_backend.service.mapper;

import com.agh.polymorphia_backend.dto.response.project.ProjectCategoryWithVariantsResponseDto;
import com.agh.polymorphia_backend.dto.response.project.ProjectVariantResponseDto;
import com.agh.polymorphia_backend.dto.response.project.ProjectVariantWithCategoryNameResponseDto;
import com.agh.polymorphia_backend.model.project.ProjectVariant;
import com.agh.polymorphia_backend.model.project.ProjectVariantCategory;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ProjectMapper {

    public ProjectVariantResponseDto toProjectVariantResponseDto(ProjectVariant projectVariant) {
        return toProjectVariantResponseDto(ProjectVariantWithCategoryNameResponseDto.builder().categoryName(projectVariant.getCategory().getName()), projectVariant);

    }

    public ProjectCategoryWithVariantsResponseDto toProjectCategoryResponseDto(ProjectVariantCategory projectVariantCategory) {
        return ProjectCategoryWithVariantsResponseDto.builder()
                .id(projectVariantCategory.getId())
                .name(projectVariantCategory.getName())
                .variants(projectVariantCategory.getVariants()
                        .stream()
                        .map(projectVariant -> toProjectVariantResponseDto(ProjectVariantResponseDto.builder(), projectVariant))
                        .toList())
                .build();
    }

    private ProjectVariantResponseDto toProjectVariantResponseDto(ProjectVariantResponseDto.ProjectVariantResponseDtoBuilder builder, ProjectVariant projectVariant) {
        return builder
                .id(projectVariant.getId())
                .name(projectVariant.getName())
                .imageUrl(projectVariant.getImageUrl())
                .shortCode(projectVariant.getShortCode())
                .build();
    }
}
