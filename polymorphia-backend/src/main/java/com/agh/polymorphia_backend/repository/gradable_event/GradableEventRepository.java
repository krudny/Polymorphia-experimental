package com.agh.polymorphia_backend.repository.gradable_event;

import com.agh.polymorphia_backend.dto.response.target_list.StudentTargetDataResponseDto;
import com.agh.polymorphia_backend.model.gradable_event.GradableEvent;
import com.agh.polymorphia_backend.repository.gradable_event.projections.GradableEventDetailsProjection;
import com.agh.polymorphia_backend.repository.gradable_event.projections.ProjectDetailsDetailsProjection;
import com.agh.polymorphia_backend.repository.gradable_event.projections.StudentGradableEventProjection;
import com.agh.polymorphia_backend.repository.gradable_event.projections.TeachingRoleGradableEventProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GradableEventRepository extends JpaRepository<GradableEvent, Long> {
    @Query("SELECT ge.eventSection.course.id FROM GradableEvent ge WHERE ge.id = :id")
    Optional<Long> getCourseIdByGradableEventId(@Param("id") Long id);

    @Query("""
    SELECT ge.id as id,
           ge.name as name,
           ge.orderIndex as orderIndex,
           ge.topic as topic,
           ge.roadMapOrderIndex as roadMapOrderIndex,
           ge.isHidden as isHidden,
           ge.isLocked as isLocked,
           (SELECT CAST(SUM(sub_cg.xp) AS BigDecimal)
              FROM CriterionGrade sub_cg
              JOIN sub_cg.grade sub_g
              WHERE sub_g.gradableEvent.id = ge.id
                AND sub_g.animal.id = :animalId) as gainedXp,
           CASE WHEN COUNT(DISTINCT cr.criterion.id) > 0 THEN true ELSE false END as hasPossibleReward,
           CASE WHEN COUNT(DISTINCT g.id) > 0 THEN true ELSE false END as isGraded,
           CASE WHEN COUNT(DISTINCT g.id) > 0 AND COUNT(DISTINCT ar.id) > 0 THEN true ELSE false END as isRewardAssigned
    FROM GradableEvent ge
    JOIN EventSection es on ge.eventSection.id = es.id
    LEFT JOIN Grade g ON g.gradableEvent.id = ge.id AND g.animal.id = :animalId
    LEFT JOIN Criterion c ON c.gradableEvent.id = ge.id
    LEFT JOIN CriterionGrade cg ON cg.grade.id = g.id AND cg.criterion.id = c.id
    LEFT JOIN CriterionReward cr ON cr.criterion.id = c.id
    LEFT JOIN AssignedReward ar ON ar.criterionGrade.id = cg.id
    WHERE ((:scope = 'COURSE' AND es.course.id = :idValue)
       OR (:scope = 'EVENT_SECTION' AND es.id = :idValue))
      AND ge.isHidden = false
      AND es.isHidden = false
      AND (:isRoadmap = false or es.isShownInRoadMap = true)
    GROUP BY ge.id, ge.name, ge.orderIndex, ge.roadMapOrderIndex, ge.isHidden, ge.isLocked, ge.topic, es.id
    ORDER BY
        CASE WHEN :sortBy = 'ORDER_INDEX' THEN ge.orderIndex END,
        CASE WHEN :sortBy = 'ROADMAP_ORDER_INDEX' THEN ge.roadMapOrderIndex END
    """)
    List<StudentGradableEventProjection> findStudentGradableEventsWithDetails(
            @Param("idValue") Long idValue,
            @Param("animalId") Long animalId,
            @Param("scope") String scope,
            @Param("sortBy") String sortBy,
            @Param("isRoadmap") Boolean isRoadmap
    );

    @Query(value = """
            SELECT
                ge.id,
                ge.name,
                ge.topic,
                ge.order_index,
                ge.road_map_order_index,
                ge.is_hidden,
                ge.is_locked,
                COUNT(DISTINCT CASE
                    WHEN scga.animal_id IS NOT NULL AND g.id IS NULL
                        THEN scga.animal_id
                END) as ungraded_students,
                CASE
                    WHEN COUNT(DISTINCT cr.criterion_id) > 0 THEN true
                    ELSE false
                END as has_possible_reward
            FROM gradable_events ge
            LEFT JOIN criteria c ON c.gradable_event_id = ge.id
            LEFT JOIN criteria_rewards cr ON cr.criterion_id = c.id
            LEFT JOIN (
                SELECT scga.animal_id, scga.course_group_id
                FROM students_course_groups scga
                JOIN course_groups cg ON cg.id = scga.course_group_id
                JOIN courses co ON co.id = cg.course_id
                JOIN event_sections es on es.course_id = co.id
                WHERE (
                    (:roleType = 'INSTRUCTOR' AND cg.teaching_role_user_id = :roleId)
                    OR (:roleType = 'COORDINATOR' AND co.coordinator_id = :roleId)
                ) AND (
                            (:scope = 'COURSE' AND co.id = :idValue)
                OR (:scope = 'EVENT_SECTION' AND es.id = :idValue)
                )
            ) scga ON true
            JOIN event_sections es ON ge.event_section_id = es.id
            JOIN courses co ON es.course_id = co.id
            LEFT JOIN grades g ON g.gradable_event_id = ge.id AND g.animal_id = scga.animal_id
            WHERE (
                (:scope = 'COURSE' AND co.id = :idValue)
                OR (:scope = 'EVENT_SECTION' AND es.id = :idValue)
            )
            GROUP BY
                ge.id,
                ge.name,
                ge.topic,
                ge.order_index,
                ge.road_map_order_index,
                ge.is_hidden,
                ge.is_locked,
                ge.event_section_id
            ORDER BY
                CASE WHEN :sortBy = 'ORDER_INDEX' THEN ge.order_index END,
                CASE WHEN :sortBy = 'ROADMAP_ORDER_INDEX' THEN ge.road_map_order_index END
            """, nativeQuery = true)
    List<TeachingRoleGradableEventProjection> findTeachingRoleGradableEventsWithDetails(
            @Param("idValue") Long idValue,
            @Param("roleId") Long roleId,
            @Param("roleType") String roleType,
            @Param("scope") String scope,
            @Param("sortBy") String sortBy
    );

    @Query("""
            select hofe.studentId      as id,
                   hofe.studentName    as fullName,
                   hofe.animalName     as animalName,
                   hofe.evolutionStage as evolutionStage,
                   hofe.groupName      as group,
                   hofe.imageUrl       as imageUrl,
                   sum(cg.xp)          as gainedXp,
                   hofe.animalId       as animalId
            from HallOfFameEntry hofe
                     left join Grade g on g.animal.id = hofe.animalId and g.gradableEvent.id = :gradableEventId
                     left join g.criteriaGrades cg
                     join StudentCourseGroupAssignment scga on scga.animal.id = hofe.animalId
            where (scga.courseGroup.teachingRoleUser.userId = :teachingRoleUserId or
                   scga.courseGroup.course.coordinator.userId = :teachingRoleUserId)
              and (:includeAllGroups = true or hofe.groupName in :groups)
              and (:searchTerm = '' or ((:searchByAnimal = true and
                                         lower(hofe.animalName) like lower(concat('%', :searchTerm, '%'))) or
                                        (:searchByStudent = true and
                                         lower(hofe.studentName) like lower(concat('%', :searchTerm, '%')))))
              and (:gradeStatus = 'ALL' or (:gradeStatus = 'GRADED' and g.id is not null) or
                   (:gradeStatus = 'UNGRADED' and g.id is null))
              and hofe.courseId = :courseId
            group by hofe.studentId, hofe.studentName, hofe.animalName, hofe.evolutionStage, hofe.groupName,
                     hofe.imageUrl, hofe.animalId
            order by case
                         when :sortBy = 'gainedXp' and :sortOrder = 'ASC' then COALESCE(sum(cg.xp), -1) end asc,
                     case
                         when :sortBy = 'gainedXp' and :sortOrder = 'DESC'
                             then COALESCE(sum(cg.xp), -1) end desc,
                     case when :sortBy = 'studentName' and :sortOrder = 'ASC' then hofe.studentName end asc,
                     case when :sortBy = 'studentName' and :sortOrder = 'DESC' then hofe.studentName end desc,
                     case when :sortBy = 'animalName' and :sortOrder = 'ASC' then hofe.animalName end asc,
                     case when :sortBy = 'animalName' and :sortOrder = 'DESC' then hofe.animalName end desc
            """)
    List<StudentTargetDataResponseDto> getStudentTargets(
            @Param("courseId") Long courseId,
            @Param("gradableEventId") Long gradableEventId,
            @Param("teachingRoleUserId") Long teachingRoleUserId,
            @Param("groups") List<String> groups,
            @Param("includeAllGroups") Boolean includeAllGroups,
            @Param("searchTerm") String searchTerm,
            @Param("searchByAnimal") Boolean searchByAnimal,
            @Param("searchByStudent") Boolean searchByStudent,
            @Param("sortBy") String sortBy,
            @Param("sortOrder") String sortOrder,
            @Param("gradeStatus") String gradeStatus
    );


    @Query(value = """
            SELECT
                ge.id as id,
                ge.key as key,
                ge.name as name,
                ge.topic as topic,
                ge.markdown_source_url as markdownSourceUrl,
                ge.is_hidden as isHidden,
                ge.is_locked as isLocked,
                ge.event_section_id as eventSectionId,
                ge.road_map_order_index as roadMapOrderIndex,
                es.is_shown_in_road_map as isShownInRoadmap,
                CASE
                    WHEN p.id IS NOT NULL THEN 'PROJECT'
                    WHEN t.id IS NOT NULL THEN 'TEST'
                    WHEN a.id IS NOT NULL THEN 'ASSIGNMENT'
                    ELSE 'UNKNOWN'
                END as type
            FROM gradable_events ge
            LEFT JOIN projects p ON ge.id = p.id
            LEFT JOIN tests t ON ge.id = t.id
            LEFT JOIN assignments a ON ge.id = a.id
            JOIN event_sections es ON ge.event_section_id = es.id
            WHERE es.course_id = :courseId
            ORDER BY ge.event_section_id, ge.order_index
            """, nativeQuery = true)
    List<GradableEventDetailsProjection> findGradableEventsByCourseId(@Param("courseId") Long courseId);

    @Query("""
            SELECT
                p.id as id,
                p.allowCrossCourseGroupProjectGroups as allowCrossCourseGroupProjectGroups
            FROM Project p
            WHERE p.eventSection.course.id = :courseId
            """)
    List<ProjectDetailsDetailsProjection> findProjectDetailsByCourseId(@Param("courseId") Long courseId);

    @Query("""
            select g.id
            from GradableEvent g
            where g.key=:key and g.eventSection.course.id=:courseId
            """)
    Long findIdByKeyAndCourseId(String key, Long courseId);

    @Query("""
                SELECT ge FROM GradableEvent ge
                WHERE ge.key IN :keys
                AND ge.eventSection.course.id = :courseId
            """)
    List<GradableEvent> findAllByKeyIn(List<String> keys, Long courseId);
}
