package com.agh.polymorphia_backend.model.project;

import com.agh.polymorphia_backend.model.gradable_event.subtypes.project.Project;
import com.agh.polymorphia_backend.model.user.TeachingRoleUser;
import com.agh.polymorphia_backend.model.user.student.Animal;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "project_groups")
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ProjectGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    @EqualsAndHashCode.Include
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "teaching_role_user_id")
    @ToString.Exclude
    @JsonIgnore
    private TeachingRoleUser teachingRoleUser;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id")
    @ToString.Exclude
    @JsonIgnore
    private Project project;


    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "project_groups_animals",
            joinColumns = @JoinColumn(name = "project_group_id"),
            inverseJoinColumns = @JoinColumn(name = "animal_id")
    )
    @ToString.Exclude
    @JsonIgnore
    @Builder.Default
    private List<Animal> animals = new ArrayList<>();


    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "project_groups_project_variants",
            joinColumns = @JoinColumn(name = "project_group_id"),
            inverseJoinColumns = @JoinColumn(name = "project_variant_id")
    )
    @ToString.Exclude
    @JsonIgnore
    @Builder.Default
    private List<ProjectVariant> projectVariants = new ArrayList<>();

}
