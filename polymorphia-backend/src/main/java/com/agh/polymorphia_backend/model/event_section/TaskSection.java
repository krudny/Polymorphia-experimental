package com.agh.polymorphia_backend.model.event_section;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "task_sections")
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@SuperBuilder
@NoArgsConstructor
public class TaskSection extends EventSection {

    @Override
    public EventSectionType getEventSectionType() {
        return EventSectionType.TASK;
    }
}
