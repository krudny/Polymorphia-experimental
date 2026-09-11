package com.agh.polymorphia_backend.model.project;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "project_variants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectVariant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty
    @Column(length = 64)
    private String key;

    @ManyToOne(optional = false)
    @JoinColumn(name = "project_variant_category_id")
    @NotNull
    private ProjectVariantCategory category;

    @NotEmpty
    private String name;

    @NotEmpty
    private String shortCode;

    @NotEmpty
    @Column(columnDefinition = "TEXT")
    private String description;

    @NotEmpty
    private String imageUrl;
}
