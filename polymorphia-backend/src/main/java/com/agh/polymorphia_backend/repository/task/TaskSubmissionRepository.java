package com.agh.polymorphia_backend.repository.task;

import com.agh.polymorphia_backend.model.gradable_event.subtypes.task.TaskSubmission;
import com.agh.polymorphia_backend.model.gradable_event.subtypes.task.TaskSubmissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface TaskSubmissionRepository extends JpaRepository<TaskSubmission, Long> {

    int countByTaskIdAndAnimalId(Long taskId, Long animalId);

    @Query(value = """
        SELECT * FROM task_submissions
        WHERE status = 'QUEUED'
        ORDER BY created_date
        LIMIT :batchSize
        FOR UPDATE SKIP LOCKED
        """, nativeQuery = true)
    List<TaskSubmission> findNextQueuedTaskSubmissionsWithLock(@Param("batchSize") int batchSize);

    @Query("""
        SELECT taskSubmission FROM TaskSubmission taskSubmission
        JOIN FETCH taskSubmission.task
        WHERE taskSubmission.id IN :taskSubmissionIds
        ORDER BY taskSubmission.createdDate
        """)
    List<TaskSubmission> findAllWithTaskByIdIn(@Param("taskSubmissionIds") List<Long> taskSubmissionIds);

    @Query("""
        SELECT taskSubmission.id FROM TaskSubmission taskSubmission
        WHERE taskSubmission.status = :status
          AND taskSubmission.isGraded = false
          AND taskSubmission.finishedAt < :threshold
        """)
    List<Long> findUnfinishedGradingTaskSubmissionIds(
        @Param("status") TaskSubmissionStatus status,
        @Param("threshold") Instant threshold
    );

    @Modifying
    @Query("""
        UPDATE TaskSubmission taskSubmission
        SET taskSubmission.passedWeight = :passedWeight,
            taskSubmission.totalWeight = :totalWeight,
            taskSubmission.passedCount = :passedCount,
            taskSubmission.totalCount = :totalCount,
            taskSubmission.score = :score,
            taskSubmission.status = TaskSubmissionStatus.COMPLETED,
            taskSubmission.errorMessage = null,
            taskSubmission.finishedAt = :now,
            taskSubmission.modifiedDate = :now,
            taskSubmission.lockedUntil = null,
            taskSubmission.leaseToken = null
        WHERE taskSubmission.id = :taskSubmissionId
          AND taskSubmission.leaseToken = :leaseToken
        """)
    int completeByIdAndLeaseToken(
        @Param("taskSubmissionId") Long taskSubmissionId,
        @Param("leaseToken") UUID leaseToken,
        @Param("passedWeight") BigDecimal passedWeight,
        @Param("totalWeight") BigDecimal totalWeight,
        @Param("passedCount") int passedCount,
        @Param("totalCount") int totalCount,
        @Param("score") BigDecimal score,
        @Param("now") Instant now
    );

    @Modifying
    @Query("""
        UPDATE TaskSubmission taskSubmission
        SET taskSubmission.status = TaskSubmissionStatus.INTERNAL_ERROR,
            taskSubmission.errorMessage = :errorMessage,
            taskSubmission.finishedAt = :now,
            taskSubmission.modifiedDate = :now,
            taskSubmission.lockedUntil = null,
            taskSubmission.leaseToken = null
        WHERE taskSubmission.id = :taskSubmissionId
          AND taskSubmission.leaseToken = :leaseToken
        """)
    int markFailedByIdAndLeaseToken(
        @Param("taskSubmissionId") Long taskSubmissionId,
        @Param("leaseToken") UUID leaseToken,
        @Param("errorMessage") String errorMessage,
        @Param("now") Instant now
    );

    @Modifying
    @Query("""
        UPDATE TaskSubmission taskSubmission
        SET taskSubmission.isGraded = true,
            taskSubmission.modifiedDate = :now
        WHERE taskSubmission.id = :taskSubmissionId
        """)
    int markGradedById(
        @Param("taskSubmissionId") Long taskSubmissionId,
        @Param("now") Instant now
    );

    @Modifying
    @Query("""
        UPDATE TaskSubmission taskSubmission
        SET taskSubmission.status = TaskSubmissionStatus.QUEUED,
            taskSubmission.modifiedDate = :now,
            taskSubmission.lockedUntil = null,
            taskSubmission.leaseToken = null
        WHERE taskSubmission.status = :status
          AND taskSubmission.lockedUntil < :now
          AND taskSubmission.processingAttempts < :maxProcessingAttempts
        """)
    int requeueExpiredWithRemainingAttempts(
        @Param("status") TaskSubmissionStatus status,
        @Param("now") Instant now,
        @Param("maxProcessingAttempts") int maxProcessingAttempts
    );

    @Modifying
    @Query("""
        UPDATE TaskSubmission taskSubmission
        SET taskSubmission.status = TaskSubmissionStatus.INTERNAL_ERROR,
            taskSubmission.errorMessage = :errorMessage,
            taskSubmission.finishedAt = :now,
            taskSubmission.modifiedDate = :now,
            taskSubmission.lockedUntil = null,
            taskSubmission.leaseToken = null
        WHERE taskSubmission.status = :status
          AND taskSubmission.lockedUntil < :now
          AND taskSubmission.processingAttempts >= :maxProcessingAttempts
        """)
    int failExpiredWithExhaustedAttempts(
        @Param("status") TaskSubmissionStatus status,
        @Param("now") Instant now,
        @Param("maxProcessingAttempts") int maxProcessingAttempts,
        @Param("errorMessage") String errorMessage
    );

    @Modifying
    @Query("""
        UPDATE TaskSubmission taskSubmission
        SET taskSubmission.status = TaskSubmissionStatus.QUEUED,
            taskSubmission.processingAttempts = taskSubmission.processingAttempts - 1,
            taskSubmission.startedAt = null,
            taskSubmission.modifiedDate = :now,
            taskSubmission.lockedUntil = null,
            taskSubmission.leaseToken = null
        WHERE taskSubmission.id = :taskSubmissionId
          AND taskSubmission.leaseToken = :leaseToken
        """)
    int releaseClaimByIdAndLeaseToken(
        @Param("taskSubmissionId") Long taskSubmissionId,
        @Param("leaseToken") UUID leaseToken,
        @Param("now") Instant now
    );
}