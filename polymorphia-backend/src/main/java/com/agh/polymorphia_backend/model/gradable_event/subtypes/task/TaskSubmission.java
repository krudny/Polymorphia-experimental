package com.agh.polymorphia_backend.model.gradable_event.subtypes.task;

import com.agh.polymorphia_backend.model.user.student.Animal;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "task_submissions",
        indexes = {
                @Index(name = "idx_task_submissions_status_created_date",
                        columnList = "status, created_date"),
                @Index(name = "ix_task_submissions_task_animal_created",
                        columnList = "task_id, animal_id, created_date DESC")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uc_task_submissions_task_animal_attempt",
                        columnNames = {"task_id", "animal_id", "user_attempt"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TaskSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    @ToString.Exclude
    private Task task;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "animal_id", nullable = false)
    @ToString.Exclude
    private Animal animal;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "language", length = 32, nullable = false)
    private TaskSupportedLanguage language;

    @NotNull
    @Column(name = "source_code", columnDefinition = "TEXT", nullable = false)
    private String sourceCode;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 16, nullable = false)
    private TaskSubmissionStatus status;

    @Column(name = "score", precision = 4, scale = 1)
    private BigDecimal score;

    @Column(name = "passed_weight", precision = 7, scale = 2)
    private BigDecimal passedWeight;

    @Column(name = "total_weight", precision = 7, scale = 2)
    private BigDecimal totalWeight;

    @Column(name = "passed_count")
    private Integer passedCount;

    @Column(name = "total_count")
    private Integer totalCount;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "user_attempt", nullable = false)
    @Builder.Default
    private Integer userAttempt = 0;

    @CreationTimestamp
    @Column(name = "created_date", nullable = false, updatable = false)
    private Instant createdDate;

    @UpdateTimestamp
    @Column(name = "modified_date")
    private Instant modifiedDate;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    @Column(name = "locked_until")
    private Instant lockedUntil;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "processing_attempts", nullable = false)
    @Builder.Default
    private Integer processingAttempts = 0;

    @NotNull
    @ColumnDefault("false")
    @Column(name = "is_graded", nullable = false)
    @Builder.Default
    private Boolean isGraded = false;

    @Column(name = "lease_token")
    private UUID leaseToken;

    @Version
    @NotNull
    @ColumnDefault("0")
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;
}