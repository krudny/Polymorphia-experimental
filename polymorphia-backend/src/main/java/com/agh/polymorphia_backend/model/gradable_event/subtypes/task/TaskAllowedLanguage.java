package com.agh.polymorphia_backend.model.gradable_event.subtypes.task;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "task_allowed_languages")
@IdClass(TaskAllowedLanguage.TaskAllowedLanguageId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class TaskAllowedLanguage {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "language", length = 32, nullable = false)
    private TaskSupportedLanguage language;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskAllowedLanguageId implements Serializable {
        private Long task;
        private TaskSupportedLanguage language;
    }
}