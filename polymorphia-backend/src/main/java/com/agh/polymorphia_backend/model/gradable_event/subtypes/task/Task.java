package com.agh.polymorphia_backend.model.gradable_event.subtypes.task;

import com.agh.polymorphia_backend.model.gradable_event.GradableEvent;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "tasks")
@Data
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class Task extends GradableEvent {

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "default_language", length = 32)
    private TaskSupportedLanguage defaultLanguage;

    @NotNull
    @Column(name = "cpu_time_limit_ms")
    private Integer cpuTimeLimitMs;

    @NotNull
    @Column(name = "wall_time_limit_ms")
    private Integer wallTimeLimitMs;

    @NotNull
    @Column(name = "memory_limit_mb")
    private Integer memoryLimitMb;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "grading_strategy", length = 16)
    private TaskGradingStrategy gradingStrategy;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "output_match", length = 32)
    private TaskOutputMatchMode outputMatchMode;
}