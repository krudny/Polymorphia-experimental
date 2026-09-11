package com.agh.polymorphia_backend.service.course.strategy;

import com.agh.polymorphia_backend.dto.request.course_import.variant.VariantDetailsRequestDto;
import com.agh.polymorphia_backend.model.project.ProjectVariant;
import com.agh.polymorphia_backend.repository.project.ProjectVariantCategoryRepository;
import com.agh.polymorphia_backend.repository.project.ProjectVariantRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Component
@AllArgsConstructor
public class ProjectVariantUpdateStrategy implements EntityUpdateStrategy<VariantDetailsRequestDto, ProjectVariant> {

    private final ProjectVariantRepository projectVariantRepository;
    private final ProjectVariantCategoryRepository projectVariantCategoryRepository;

    @Override
    public Function<VariantDetailsRequestDto, String> getKeyExtractor() {
        return VariantDetailsRequestDto::getKey;
    }

    @Override
    public Function<ProjectVariant, String> getEntityKeyExtractor() {
        return ProjectVariant::getKey;
    }

    @Override
    public Function<VariantDetailsRequestDto, ?> getTypeExtractor() {
        return null;
    }

    @Override
    public JpaRepository<ProjectVariant, Long> getRepository() {
        return projectVariantRepository;
    }

    @Override
    public List<ProjectVariant> findAllByKeys(List<String> keys, Long courseId) {
        return projectVariantRepository.findAllByKeyIn(keys, courseId);
    }


    @Override
    public ProjectVariant createNewEntity(VariantDetailsRequestDto dto) {
        return new ProjectVariant();
    }

    @Override
    public ProjectVariant updateEntity(ProjectVariant entity, VariantDetailsRequestDto dto, Map<VariantDetailsRequestDto, Long> orderIds, Long projectVariantCategoryId) {
        entity.setKey(dto.getKey());
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setCategory(projectVariantCategoryRepository.getReferenceById(projectVariantCategoryId));
        entity.setShortCode(dto.getShortCode());
        entity.setImageUrl(dto.getImageUrl());
        return entity;
    }
}
