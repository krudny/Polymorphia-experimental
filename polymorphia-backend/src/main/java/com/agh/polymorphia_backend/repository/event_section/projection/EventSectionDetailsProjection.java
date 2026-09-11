package com.agh.polymorphia_backend.repository.event_section.projection;

import com.agh.polymorphia_backend.model.event_section.EventSectionType;

public interface EventSectionDetailsProjection {
    Long getId();

    String getKey();

    String getName();

    Boolean getIsShownInRoadMap();

    Boolean getHasGradableEventsWithTopics();

    Boolean getIsHidden();

    EventSectionType getEventSectionType();
}
