package com.agh.polymorphia_backend.service.course.strategy;

import com.agh.polymorphia_backend.dto.request.course_import.EvolutionStageDetailsRequestDto;
import com.agh.polymorphia_backend.model.user.student.EvolutionStage;
import com.agh.polymorphia_backend.repository.course.CourseRepository;
import com.agh.polymorphia_backend.repository.course.EvolutionStagesRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Component
@AllArgsConstructor
public class EvolutionStageUpdateStrategy
        implements EntityUpdateStrategy<EvolutionStageDetailsRequestDto, EvolutionStage> {

    private final EvolutionStagesRepository evolutionStagesRepository;
    private final CourseRepository courseRepository;

    @Override
    public Function<EvolutionStageDetailsRequestDto, String> getKeyExtractor() {
        return EvolutionStageDetailsRequestDto::getKey;
    }

    @Override
    public Function<EvolutionStage, String> getEntityKeyExtractor() {
        return EvolutionStage::getKey;
    }

    @Override
    public Function<EvolutionStageDetailsRequestDto, ?> getTypeExtractor() {
        return null;
    }

    @Override
    public JpaRepository<EvolutionStage, Long> getRepository() {
        return evolutionStagesRepository;
    }

    @Override
    public List<EvolutionStage> findAllByKeys(List<String> keys, Long courseId) {
        return evolutionStagesRepository.findAllByKeyIn(keys, courseId);
    }

    @Override
    public EvolutionStage createNewEntity(EvolutionStageDetailsRequestDto dto) {
        return new EvolutionStage();
    }

    @Override
    public EvolutionStage updateEntity(EvolutionStage entity, EvolutionStageDetailsRequestDto dto,
                                       Map<EvolutionStageDetailsRequestDto, Long> orderIds,
                                       Long courseId) {
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setGrade(dto.getGrade());
        entity.setMinXp(dto.getMinXp());
        entity.setImageUrl(dto.getImageUrl());
        entity.setOrderIndex(orderIds.get(dto));
        entity.setKey(dto.getKey());

        if (entity.getId() == null) {
            entity.setCourse(courseRepository.getReferenceById(courseId));
        }
        return entity;
    }
}