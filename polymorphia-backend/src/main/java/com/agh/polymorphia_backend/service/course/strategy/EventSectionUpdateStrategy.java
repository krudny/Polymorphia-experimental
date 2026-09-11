package com.agh.polymorphia_backend.service.course.strategy;

import com.agh.polymorphia_backend.dto.request.course_import.event_section.EventSectionDetailsRequestDto;
import com.agh.polymorphia_backend.model.event_section.*;
import com.agh.polymorphia_backend.repository.course.CourseRepository;
import com.agh.polymorphia_backend.repository.event_section.EventSectionRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Component
@AllArgsConstructor
public class EventSectionUpdateStrategy implements EntityUpdateStrategy<EventSectionDetailsRequestDto, EventSection> {

    private final EventSectionRepository eventSectionRepository;
    private final CourseRepository courseRepository;

    @Override
    public Function<EventSectionDetailsRequestDto, String> getKeyExtractor() {
        return EventSectionDetailsRequestDto::getKey;
    }

    @Override
    public Function<EventSection, String> getEntityKeyExtractor() {
        return EventSection::getKey;
    }

    @Override
    public Function<EventSectionDetailsRequestDto, ?> getTypeExtractor() {
        return EventSectionDetailsRequestDto::getType;
    }

    @Override
    public JpaRepository<EventSection, Long> getRepository() {
        return eventSectionRepository;
    }

    @Override
    public List<EventSection> findAllByKeys(List<String> keys, Long courseId) {
        return eventSectionRepository.findAllByKeyIn(keys, courseId);
    }

    @Override
    public EventSection createNewEntity(EventSectionDetailsRequestDto dto) {
        return switch (dto.getType()) {
            case ASSIGNMENT -> new AssignmentSection();
            case TEST -> new TestSection();
            case PROJECT -> new ProjectSection();
            case TASK -> new TaskSection();
        };
    }

    @Override
    public EventSection updateEntity(EventSection entity, EventSectionDetailsRequestDto dto, Map<EventSectionDetailsRequestDto, Long> orderIds, Long courseId) {
        entity.setName(dto.getName());
        entity.setKey(dto.getKey());
        entity.setOrderIndex(orderIds.get(dto));
        entity.setHasGradableEventsWithTopics(dto.getEventsWithTopics());
        entity.setShownInRoadMap(dto.getIsShownInRoadmap());
        entity.setIsHidden(dto.getIsHidden());

        if (entity.getId() == null) {
            entity.setCourse(courseRepository.getReferenceById(courseId));
        }

        return entity;
    }
}
