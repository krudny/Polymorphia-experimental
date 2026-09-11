package com.agh.polymorphia_backend.service.course.strategy;

import com.agh.polymorphia_backend.dto.request.course_import.criterion.CriterionDetailsRequestDto;
import com.agh.polymorphia_backend.model.criterion.Criterion;
import com.agh.polymorphia_backend.repository.criterion.CriterionRepository;
import com.agh.polymorphia_backend.repository.gradable_event.GradableEventRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Component
@AllArgsConstructor
public class CriterionUpdateStrategy implements EntityUpdateStrategy<CriterionDetailsRequestDto, Criterion> {

    private final CriterionRepository criterionRepository;
    private final GradableEventRepository gradableEventRepository;

    @Override
    public Function<CriterionDetailsRequestDto, String> getKeyExtractor() {
        return CriterionDetailsRequestDto::getKey;
    }

    @Override
    public Function<Criterion, String> getEntityKeyExtractor() {
        return Criterion::getKey;
    }

    @Override
    public Function<CriterionDetailsRequestDto, ?> getTypeExtractor() {
        return null;
    }

    @Override
    public JpaRepository<Criterion, Long> getRepository() {
        return criterionRepository;
    }

    @Override
    public List<Criterion> findAllByKeys(List<String> keys, Long courseId) {
        return criterionRepository.findAllByKeyIn(keys, courseId);
    }

    @Override
    public Criterion createNewEntity(CriterionDetailsRequestDto dto) {
        return new Criterion();
    }

    @Override
    public Criterion updateEntity(Criterion entity, CriterionDetailsRequestDto dto, Map<CriterionDetailsRequestDto, Long> orderIds, Long gradableEventId) {
        entity.setKey(dto.getKey());
        entity.setName(dto.getName());
        entity.setMaxXp(dto.getMaxXp());
        entity.setGradableEvent(gradableEventRepository.getReferenceById(gradableEventId));

        return entity;
    }
}
