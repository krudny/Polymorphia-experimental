package com.agh.polymorphia_backend.model.gradable_event;

import com.agh.polymorphia_backend.model.criterion.Criterion;
import com.agh.polymorphia_backend.model.event_section.EventSection;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "gradable_events")
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Inheritance(strategy = InheritanceType.JOINED)
public class GradableEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    @EqualsAndHashCode.Include
    private Long id;

    @NotEmpty
    @Column(length = 64)
    private String key;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_section_id")
    @ToString.Exclude
    @JsonIgnore
    private EventSection eventSection;

    @NotNull
    @OneToMany(mappedBy = "gradableEvent", fetch = FetchType.LAZY)
    @ToString.Exclude
    @JsonIgnore
    @Builder.Default
    private List<Criterion> criteria = new ArrayList<>();

    @NotNull
    @Column(length = 64)
    private String name;

    @Column(length = 64)
    private String topic;

    @NotNull
    private Long orderIndex;

    private Long roadMapOrderIndex;

    @Column(length = 128)
    private String markdownSourceUrl;

    private String markdown;

    @NotNull
    private Boolean isHidden;

    @NotNull
    private Boolean isLocked;
}
