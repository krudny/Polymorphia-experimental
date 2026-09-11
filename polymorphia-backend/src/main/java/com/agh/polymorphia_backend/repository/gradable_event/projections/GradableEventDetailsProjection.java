package com.agh.polymorphia_backend.repository.gradable_event.projections;

import com.agh.polymorphia_backend.model.event_section.EventSectionType;

public interface GradableEventDetailsProjection {
    Long getId();

    String getKey();

    String getName();

    String getTopic();

    String getMarkdownSourceUrl();

    Boolean getIsHidden();

    Boolean getIsLocked();

    Long getEventSectionId();

    Boolean getIsShownInRoadmap();

    EventSectionType getType();

    Long getRoadmapOrderIndex();
}
