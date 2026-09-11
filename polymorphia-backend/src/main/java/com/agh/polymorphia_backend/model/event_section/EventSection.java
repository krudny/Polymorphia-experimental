package com.agh.polymorphia_backend.model.event_section;

import com.agh.polymorphia_backend.model.course.Course;
import com.agh.polymorphia_backend.model.gradable_event.GradableEvent;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "event_sections")
@Inheritance(strategy = InheritanceType.JOINED)

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class EventSection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    @EqualsAndHashCode.Include
    private Long id;

    @NotEmpty
    @Column(length = 64)
    private String key;

    @NotEmpty
    private String name;

    @NotNull
    private boolean isShownInRoadMap = false;

    @NotNull
    private boolean hasGradableEventsWithTopics = false;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    @NotNull
    @PositiveOrZero
    private Long orderIndex;

    @OneToMany(mappedBy = "eventSection", fetch = FetchType.LAZY, orphanRemoval = true)
    @ToString.Exclude
    @JsonIgnore
    @Builder.Default
    private List<GradableEvent> gradableEvents = new ArrayList<>();

    @NotNull
    private Boolean isHidden = false;

    public abstract EventSectionType getEventSectionType();
}
