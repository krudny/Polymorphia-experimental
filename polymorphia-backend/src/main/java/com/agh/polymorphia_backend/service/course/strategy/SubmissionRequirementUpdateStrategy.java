package com.agh.polymorphia_backend.service.course.strategy;

import com.agh.polymorphia_backend.dto.request.course_import.SubmissionRequirementDetailsRequestDto;
import com.agh.polymorphia_backend.model.submission.SubmissionRequirement;
import com.agh.polymorphia_backend.repository.gradable_event.GradableEventRepository;
import com.agh.polymorphia_backend.repository.submission.SubmissionRequirementRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Component
@AllArgsConstructor
public class SubmissionRequirementUpdateStrategy implements EntityUpdateStrategy<SubmissionRequirementDetailsRequestDto, SubmissionRequirement> {


    private final SubmissionRequirementRepository submissionRequirementRepository;
    private final GradableEventRepository gradableEventRepository;

    @Override
    public Function<SubmissionRequirementDetailsRequestDto, String> getKeyExtractor() {
        return SubmissionRequirementDetailsRequestDto::getKey;
    }

    @Override
    public Function<SubmissionRequirement, String> getEntityKeyExtractor() {
        return SubmissionRequirement::getKey;
    }

    @Override
    public Function<SubmissionRequirementDetailsRequestDto, ?> getTypeExtractor() {
        return null;
    }

    @Override
    public JpaRepository<SubmissionRequirement, Long> getRepository() {
        return submissionRequirementRepository;
    }

    @Override
    public List<SubmissionRequirement> findAllByKeys(List<String> keys, Long courseId) {
        return submissionRequirementRepository.findAllByKeyIn(keys, courseId);
    }

    @Override
    public SubmissionRequirement createNewEntity(SubmissionRequirementDetailsRequestDto dto) {
        return new SubmissionRequirement();
    }

    @Override
    public SubmissionRequirement updateEntity(SubmissionRequirement entity, SubmissionRequirementDetailsRequestDto dto, Map<SubmissionRequirementDetailsRequestDto, Long> orderIds, Long gradableEventId) {
        entity.setKey(dto.getKey());
        entity.setName(dto.getName());
        entity.setMandatory(dto.getIsMandatory());
        entity.setGradableEvent(gradableEventRepository.getReferenceById(gradableEventId));
        entity.setOrderIndex(orderIds.get(dto));

        return entity;
    }
}
