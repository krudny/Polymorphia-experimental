package com.agh.polymorphia_backend.repository.criterion;

import com.agh.polymorphia_backend.model.criterion.Criterion;
import com.agh.polymorphia_backend.repository.criterion.projection.CriterionDetailsProjection;
import com.agh.polymorphia_backend.repository.criterion.projection.CriterionRewardDetailsProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CriterionRepository extends JpaRepository<Criterion, Long> {
    @Query(value = """
            SELECT
                c.id,
                c.name,
                c.max_xp,
                c.gradable_event_id,
                c.key
            FROM criteria c
            JOIN gradable_events ge ON c.gradable_event_id = ge.id
            JOIN event_sections es ON ge.event_section_id = es.id
            WHERE es.course_id = :courseId
            ORDER BY c.gradable_event_id
            """, nativeQuery = true)
    List<CriterionDetailsProjection> findByCourseId(@Param("courseId") Long courseId);

    @Query(value = """
            SELECT
                cr.criterion_id,
                r.key as reward_key,
                cr.max_amount
            FROM criteria_rewards cr
            JOIN rewards r ON cr.reward_id = r.id
            JOIN criteria c ON cr.criterion_id = c.id
            JOIN gradable_events ge ON c.gradable_event_id = ge.id
            JOIN event_sections es ON ge.event_section_id = es.id
            WHERE es.course_id = :courseId
            """, nativeQuery = true)
    List<CriterionRewardDetailsProjection> findCriteriaRewardsByCourseId(@Param("courseId") Long courseId);

    @Query("""
                SELECT c FROM Criterion c
                WHERE c.key IN :keys
                AND c.gradableEvent.eventSection.course.id = :courseId
            """)
    List<Criterion> findAllByKeyIn(List<String> keys, Long courseId);

    @Query("""
            select c.id
            from Criterion c
            where c.key=:key and c.gradableEvent.eventSection.course.id=:courseId
            """)
    Long findIdByKeyAndCourseId(String key, Long courseId);
}
