package com.agh.polymorphia_backend.model.reward;

import com.agh.polymorphia_backend.model.course.Course;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Inheritance(strategy = InheritanceType.JOINED)
@NoArgsConstructor
@SuperBuilder
@Table(name = "rewards")
public abstract class Reward {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @NotEmpty
    @Column(length = 64)
    private String key;

    @NotEmpty
    private String name;

    @NotEmpty
    @Column(length = 1000)
    private String description;

    @NotEmpty
    private String imageUrl;

    @NotNull
    @PositiveOrZero
    private Long orderIndex;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    public abstract RewardType getRewardType();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Reward other)) return false;
        return getId() != null && getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : 0;
    }
}
