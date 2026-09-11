package com.agh.polymorphia_backend.repository.submission;

import com.agh.polymorphia_backend.model.gradable_event.GradableEvent;
import com.agh.polymorphia_backend.model.submission.SubmissionRequirement;
import com.agh.polymorphia_backend.repository.submission.projection.SubmissionRequirementDetailsProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubmissionRequirementRepository extends JpaRepository<SubmissionRequirement, Long> {
    List<SubmissionRequirement> getSubmissionRequirementsByGradableEvent(GradableEvent gradableEvent);

    @Query(value = """
            SELECT
                sr.id,
                sr.name,
                sr.is_mandatory,
                sr.gradable_event_id,
                sr.key
            FROM submission_requirements sr
            JOIN gradable_events ge ON sr.gradable_event_id = ge.id
            JOIN event_sections es ON ge.event_section_id = es.id
            WHERE es.course_id = :courseId
            ORDER BY sr.order_index
            """, nativeQuery = true)
    List<SubmissionRequirementDetailsProjection> findByCourseId(@Param("courseId") Long courseId);

    @Query("""
                SELECT sr FROM SubmissionRequirement sr
                WHERE sr.key IN :keys
                AND sr.gradableEvent.eventSection.course.id = :courseId
            """)
    List<SubmissionRequirement> findAllByKeyIn(List<String> keys, Long courseId);
}
