package com.agh.polymorphia_backend.repository.grade;

import com.agh.polymorphia_backend.model.grade.Grade;
import com.agh.polymorphia_backend.model.project.ProjectGroup;
import com.agh.polymorphia_backend.repository.grade.projections.StudentActivityProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GradeRepository extends JpaRepository<Grade, Long> {

    List<Grade> findAllByAnimalId(Long animalId);

    @Query(
            """
                    select g
                    from Grade g
                    where g.animal.id = :animalId
                    AND g.gradableEvent.id = :gradableEventId
                    """
    )
    Optional<Grade> findByAnimalIdAndGradableEventId(Long animalId, Long gradableEventId);

    @Query(value = """
        select
            g.animal_id as animalId,
            ge.id       as id,
            ge.name     as gradableEventName,
            sum(cg.xp)  as gainedXp,
            case
                when exists (
                    select *
                    from assigned_rewards ar
                             join criteria_grades cg2 on cg2.id = ar.criterion_grade_id
                    where cg2.grade_id = g.id
                )
                    then true
                else false
                end        as hasReward,
            g.modified_date as gradeDate
        from grades g
                 join gradable_events ge on g.gradable_event_id = ge.id
                 join criteria_grades cg on g.id = cg.grade_id
        where g.animal_id = :animalId
        group by g.id, g.animal_id, ge.id, ge.name, g.modified_date
        order by g.modified_date desc
    """, nativeQuery = true)
    List<StudentActivityProjection> findStudentActivity(Long animalId);

    @Query("""                                                                                                                                                                                                                                                                                                                                                                                                                      
        select case when count(g) > 0 then true else false end
        from Grade g
        where g.gradableEvent.id = :#{#projectGroup.project.id}
            and g.animal.id in (
              select a.id
              from ProjectGroup pg
                   join pg.animals a
              where pg = :projectGroup
            )
      """)
    boolean hasGroupGrades(@Param("projectGroup") ProjectGroup projectGroup);
}
