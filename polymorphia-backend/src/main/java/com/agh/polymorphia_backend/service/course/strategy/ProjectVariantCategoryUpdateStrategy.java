package com.agh.polymorphia_backend.service.course.strategy;

import com.agh.polymorphia_backend.dto.request.course_import.variant.VariantCategoryDetailsRequestDto;
import com.agh.polymorphia_backend.model.project.ProjectVariantCategory;
import com.agh.polymorphia_backend.repository.project.ProjectRepository;
import com.agh.polymorphia_backend.repository.project.ProjectVariantCategoryRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Component
@AllArgsConstructor
public class ProjectVariantCategoryUpdateStrategy implements EntityUpdateStrategy<VariantCategoryDetailsRequestDto, ProjectVariantCategory> {

    private final ProjectVariantCategoryRepository projectVariantCategoryRepository;
    private final ProjectRepository projectRepository;

    @Override
    public Function<VariantCategoryDetailsRequestDto, String> getKeyExtractor() {
        return VariantCategoryDetailsRequestDto::getKey;
    }

    @Override
    public Function<ProjectVariantCategory, String> getEntityKeyExtractor() {
        return ProjectVariantCategory::getKey;
    }

    @Override
    public Function<VariantCategoryDetailsRequestDto, ?> getTypeExtractor() {
        return null;
    }

    @Override
    public JpaRepository<ProjectVariantCategory, Long> getRepository() {
        return projectVariantCategoryRepository;
    }

    @Override
    public List<ProjectVariantCategory> findAllByKeys(List<String> keys, Long courseId) {
        return projectVariantCategoryRepository.findAllByKeyIn(keys, courseId);
    }

    @Override
    public ProjectVariantCategory createNewEntity(VariantCategoryDetailsRequestDto dto) {
        return new ProjectVariantCategory();
    }

    @Override
    public ProjectVariantCategory updateEntity(ProjectVariantCategory entity, VariantCategoryDetailsRequestDto dto, Map<VariantCategoryDetailsRequestDto, Long> orderIds, Long gradableEventId) {
        entity.setKey(dto.getKey());
        entity.setName(dto.getName());
        entity.setProject(projectRepository.getReferenceById(gradableEventId));

        return entity;
    }
}
