package com.agh.polymorphia_backend.model.criterion;

import com.agh.polymorphia_backend.model.gradable_event.GradableEvent;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "criteria")
@Data
@Builder
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Criterion {
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
    @JoinColumn(name = "gradable_event_id")
    @ToString.Exclude
    @JsonIgnore
    private GradableEvent gradableEvent;

    @OneToMany(mappedBy = "criterion", fetch = FetchType.LAZY, orphanRemoval = true)
    @ToString.Exclude
    @JsonIgnore
    @Builder.Default
    private List<CriterionReward> assignableRewards = new ArrayList<>();

    @NotEmpty
    @Column(length = 64)
    private String name;

    @NotNull
    @Column(precision = 4, scale = 1)
    private BigDecimal maxXp;
}
