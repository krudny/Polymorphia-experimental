package com.agh.polymorphia_backend.service.course.strategy;

import com.agh.polymorphia_backend.dto.request.course_import.gradable_event.GradableEventDetailsRequestDto;
import com.agh.polymorphia_backend.dto.request.course_import.gradable_event.ProjectDetailsRequestDto;
import com.agh.polymorphia_backend.model.event_section.EventSectionType;
import com.agh.polymorphia_backend.model.gradable_event.*;
import com.agh.polymorphia_backend.model.gradable_event.subtypes.assignment.Assignment;
import com.agh.polymorphia_backend.model.gradable_event.subtypes.project.Project;
import com.agh.polymorphia_backend.model.gradable_event.subtypes.task.Task;
import com.agh.polymorphia_backend.model.gradable_event.subtypes.test.Test;
import com.agh.polymorphia_backend.repository.event_section.EventSectionRepository;
import com.agh.polymorphia_backend.repository.gradable_event.GradableEventRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Component
@AllArgsConstructor
public class GradableEventUpdateStrategy implements EntityUpdateStrategy<GradableEventDetailsRequestDto, GradableEvent> {

    private final GradableEventRepository gradableEventRepository;
    private final EventSectionRepository eventSectionRepository;

    @Override
    public Function<GradableEventDetailsRequestDto, String> getKeyExtractor() {
        return GradableEventDetailsRequestDto::getKey;
    }

    @Override
    public Function<GradableEvent, String> getEntityKeyExtractor() {
        return GradableEvent::getKey;
    }

    @Override
    public Function<GradableEventDetailsRequestDto, ?> getTypeExtractor() {
        return GradableEventDetailsRequestDto::getType;
    }

    @Override
    public JpaRepository<GradableEvent, Long> getRepository() {
        return gradableEventRepository;
    }

    @Override
    public List<GradableEvent> findAllByKeys(List<String> keys, Long courseId) {
        return gradableEventRepository.findAllByKeyIn(keys, courseId);
    }

    @Override
    public GradableEvent createNewEntity(GradableEventDetailsRequestDto dto) {
        return switch (dto.getType()) {
            case ASSIGNMENT -> new Assignment();
            case TEST -> new Test();
            case PROJECT -> new Project();
            case TASK -> new Task();
        };
    }

    @Override
    public GradableEvent updateEntity(GradableEvent entity, GradableEventDetailsRequestDto dto, Map<GradableEventDetailsRequestDto, Long> orderIds, Long eventSectionId) {
        entity.setKey(dto.getKey());
        entity.setName(dto.getName());
        entity.setTopic(dto.getTopic());
        entity.setIsHidden(dto.getIsHidden());
        entity.setIsLocked(dto.getIsLocked());
        entity.setMarkdownSourceUrl(dto.getMarkdownSourceUrl());
        entity.setOrderIndex(orderIds.get(dto));
        entity.setEventSection(eventSectionRepository.getReferenceById(eventSectionId));

        if (dto.getType().equals(EventSectionType.PROJECT)) {
            ((Project) entity).setAllowCrossCourseGroupProjectGroups(((ProjectDetailsRequestDto) dto).getAllowCrossCourseGroupProjectGroup());
        }
        return entity;
    }


}
