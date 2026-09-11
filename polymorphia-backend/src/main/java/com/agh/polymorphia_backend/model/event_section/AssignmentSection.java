package com.agh.polymorphia_backend.model.event_section;

import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "assignment_sections")
@Data
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class AssignmentSection extends EventSection {
    @Override
    public EventSectionType getEventSectionType() {
        return EventSectionType.ASSIGNMENT;
    }
}
