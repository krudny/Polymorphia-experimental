package com.agh.polymorphia_backend.model.course;

import com.agh.polymorphia_backend.model.user.coordinator.Coordinator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "courses")
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    @EqualsAndHashCode.Include
    private Long id;

    @NotEmpty
    private String name;

    @NotEmpty
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coordinator_id")
    private Coordinator coordinator;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "course")
    @ToString.Exclude
    @JsonIgnore
    @Builder.Default
    private List<CourseGroup> courseGroups = new ArrayList<>();

    @Column(length = 128)
    private String markdownSourceUrl;

    private String markdown;

    @NotEmpty
    private String coordinatorImageUrl;

    @NotEmpty
    private String instructorImageUrl;
}
