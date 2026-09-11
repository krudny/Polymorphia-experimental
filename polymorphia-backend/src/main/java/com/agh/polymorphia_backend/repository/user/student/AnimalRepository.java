package com.agh.polymorphia_backend.repository.user.student;

import com.agh.polymorphia_backend.model.user.student.Animal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnimalRepository extends JpaRepository<Animal, Long> {
    @Query("select a from Animal a join a.studentCourseGroupAssignment cga  join cga.courseGroup c " +
            "where cga.student.user.id=:studentId AND c.course.id=:courseId")
    Optional<Animal> findByCourseIdAndStudentId(Long courseId, Long studentId);

    @Query("select a.id from Animal a join a.studentCourseGroupAssignment cga  join cga.courseGroup c " +
            "where cga.student.user.id=:studentId AND c.course.id=:courseId")
    Optional<Long> findIdByCourseIdAndStudentId(Long courseId, Long studentId);

    @Query("""
            select count(scga) > 0
            from StudentCourseGroupAssignment scga
            where scga.student.userId = :studentId AND scga.courseGroup.course.id = :courseId
            """)
    boolean existsByCourseIdAndStudentId(Long courseId, Long studentId);

    @Query("select a from Animal a join a.studentCourseGroupAssignment cga  join cga.courseGroup c " +
            "where cga.student.user.id in :studentIds AND c.course.id=:courseId")
    List<Animal> findByCourseIdAndStudentIds(Long courseId, List<Long> studentIds);


    @Query("""
                select a from Animal a
                    where a.name = :name AND a.studentCourseGroupAssignment.courseGroup.course.id = :courseId
            """)
    Optional<Animal> findByNameAndCourseId(String name, Long courseId);


    @Query(value = """
                select a.id from animals a
                    join grades g on g.animal_id = a.id
                    join criteria_grades cg on g.id = cg.grade_id
                    join assigned_rewards ar on cg.id = ar.criterion_grade_id
                    where ar.id = :assignedChestId
            """, nativeQuery = true)
    Long findByAssignedChestId(Long assignedChestId);

    @Query(value = """
                select count(distinct cg.id) from Animal a
                    join a.studentCourseGroupAssignment scga
                    join scga.courseGroup cg
                    where a in :animals
    """)
    Long countAnimalCourseGroups(List<Animal> animals);

    @Modifying
    @Query(value = """
    DELETE FROM animals a
    WHERE EXISTS (
        SELECT 1
        FROM students_course_groups scg
        WHERE scg.animal_id = a.id
        AND scg.course_group_id = :courseGroupId
    )
    """, nativeQuery = true)
    void deleteAnimalsByCourseGroupId(@Param("courseGroupId") Long courseGroupId);

    @Modifying
    @Query("DELETE FROM Animal a WHERE a.id = :id")
    void deleteAnimalById(@Param("id") Long id);
}
