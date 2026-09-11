package com.agh.polymorphia_backend.model.project;

import com.agh.polymorphia_backend.model.gradable_event.subtypes.project.Project;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "project_variant_categories")
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ProjectVariantCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty
    @Column(length = 64)
    private String key;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "project_id")
    @ToString.Exclude
    @JsonIgnore
    private Project project;

    @NotEmpty
    private String name;

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    @ToString.Exclude
    @JsonIgnore
    private List<ProjectVariant> variants = new ArrayList<>();
}