package com.agh.polymorphia_backend.model.gradable_event.subtypes.assignment;

import com.agh.polymorphia_backend.model.gradable_event.GradableEvent;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "assignments")
@Data
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class Assignment extends GradableEvent {
}