package com.agh.polymorphia_backend.repository.event_section;

import com.agh.polymorphia_backend.model.event_section.EventSection;
import com.agh.polymorphia_backend.repository.event_section.projection.EventSectionDetailsProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface EventSectionRepository extends JpaRepository<EventSection, Long> {
    List<EventSection> findByIdIn(Set<Long> ids);

    @Query("""
            select es.id
            from EventSection es
            where es.key=:key and es.course.id=:courseId
            """)
    Long findIdByKeyAndCourseId(String key, Long courseId);

    boolean existsByCourseIdAndName(Long courseId, String name);

    @Query("SELECT es.course.id FROM EventSection es WHERE es.id = :eventSectionId")
    Optional<Long> findCourseIdById(@Param("eventSectionId") Long eventSectionId);

    @Query("""
        SELECT es FROM EventSection es
        WHERE es.course.id = :courseId
        ORDER BY es.orderIndex ASC
    """)
    List<EventSection> findByCourseIdWithHidden(@Param("courseId") Long courseId);

    @Query("""
        SELECT DISTINCT es
        FROM EventSection es
        WHERE es.course.id = :courseId
          AND es.isHidden = false
          AND EXISTS (
              SELECT 1
              FROM GradableEvent ge
              WHERE ge.eventSection.id = es.id
                AND ge.isHidden = false
          )
        ORDER BY es.orderIndex ASC
    """)
    List<EventSection> findByCourseIdWithoutHidden(@Param("courseId") Long courseId);

    @Query("""
    SELECT es
    FROM EventSection es
    WHERE es.id = :eventSectionId
      AND (
          :userRole <> 'STUDENT'
          OR (
              es.isHidden = false
              AND EXISTS (
                  SELECT 1
                  FROM GradableEvent g
                  WHERE g.eventSection.id = es.id
                    AND g.isHidden = false
              )
          )
      )
    """)
    Optional<EventSection> findByIdWithVisibilityCheck(
            @Param("eventSectionId") Long eventSectionId,
            @Param("userRole") String userRole
    );

    @Query(value = """
            SELECT
                es.id,
                es.key,
                es.name,
                es.is_shown_in_road_map,
                es.has_gradable_events_with_topics,
                es.is_hidden,
                CASE
                    WHEN ases.id IS NOT NULL THEN 'ASSIGNMENT'
                    WHEN tses.id IS NOT NULL THEN 'TEST'
                    WHEN pses.id IS NOT NULL THEN 'PROJECT'
                END as event_section_type
            FROM event_sections es
            LEFT JOIN assignment_sections ases ON es.id = ases.id
            LEFT JOIN test_sections tses ON es.id = tses.id
            LEFT JOIN project_sections pses ON es.id = pses.id
            WHERE es.course_id = :courseId
            ORDER BY es.order_index
            """, nativeQuery = true)
    List<EventSectionDetailsProjection> findBasicByCourseId(@Param("courseId") Long courseId);

    @Query("""
                SELECT e FROM EventSection e
                WHERE e.key IN :keys
                AND e.course.id = :courseId
            """)
    List<EventSection> findAllByKeyIn(List<String> keys, Long courseId);
}
