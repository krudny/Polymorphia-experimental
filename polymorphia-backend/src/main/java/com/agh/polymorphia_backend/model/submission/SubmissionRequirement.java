package com.agh.polymorphia_backend.model.submission;

import com.agh.polymorphia_backend.model.gradable_event.GradableEvent;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

@Entity
@Table(name = "submission_requirements")
@Data
@Builder
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SubmissionRequirement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "gradable_event_id")
    @ToString.Exclude
    @JsonIgnore
    private GradableEvent gradableEvent;

    @NotEmpty
    @Column(length = 64)
    private String key;

    @NotEmpty
    private String name;

    @NotNull
    private boolean isMandatory = true;

    @NotNull
    @PositiveOrZero
    private Long orderIndex;
}
