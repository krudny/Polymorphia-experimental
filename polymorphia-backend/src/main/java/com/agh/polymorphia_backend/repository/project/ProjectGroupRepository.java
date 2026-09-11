package com.agh.polymorphia_backend.repository.project;

import com.agh.polymorphia_backend.dto.response.user_context.StudentDetailsResponseDto;
import com.agh.polymorphia_backend.model.project.ProjectGroup;
import com.agh.polymorphia_backend.model.user.student.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectGroupRepository extends JpaRepository<ProjectGroup, Long> {
    @Query("""
             SELECT pg FROM ProjectGroup pg
                 JOIN pg.animals a
                 WHERE a.studentCourseGroupAssignment.student.userId = :studentId AND pg.project.id = :projectId
            """)
    Optional<ProjectGroup> getProjectGroupByStudentIdAndProjectId(@Param("studentId") Long studentId,
                                                                  @Param("projectId") Long projectId);

    @Query("""
             SELECT pg FROM ProjectGroup pg
                 WHERE pg.id = :id AND pg.project.id = :projectId
            """)
    Optional<ProjectGroup> getProjectGroupByIdAndProjectId(@Param("id") Long id, @Param("projectId") Long projectId);

    @Query("""
             SELECT pg FROM ProjectGroup pg
                 JOIN pg.animals a
                 WHERE a.studentCourseGroupAssignment.student.userId = :studentId
                    AND pg.id = :id
                    AND pg.project.id = :projectId
            """)
    Optional<ProjectGroup> getProjectGroupByIdAndStudentIdAndProjectId(@Param("id") Long id,
                                                                       @Param("studentId") Long studentId,
                                                                       @Param("projectId") Long projectId);

    @Query("""
             SELECT pg FROM ProjectGroup pg
                 JOIN pg.animals a
                 WHERE a.studentCourseGroupAssignment.student.userId = :studentId
                    AND pg.project.id = :projectId
                    AND pg.teachingRoleUser.userId = :teachingRoleUserId
            """)
    Optional<ProjectGroup> getProjectGroupByStudentIdAndProjectIdAndTeachingRoleUserId(
            @Param("studentId") Long studentId, @Param("projectId") Long projectId,
            @Param("teachingRoleUserId") Long teachingRoleUserId);

    @Query("""
             SELECT pg FROM ProjectGroup pg
                 WHERE pg.id = :id AND pg.project.id = :projectId AND pg.teachingRoleUser.userId = :teachingRoleUserId
            """)
    Optional<ProjectGroup> getProjectGroupByIdAndProjectIdAndTeachingRoleUserId(@Param("id") Long id,
                                                                                @Param("projectId") Long projectId,
                                                                                @Param("teachingRoleUserId")
                                                                                Long teachingRoleUserId);

    @Query("""
            select pg from ProjectGroup pg
                join pg.animals a
                join a.studentCourseGroupAssignment scga
                where pg.project.id = :projectId and scga.student.userId = :studentId
    """)
    Optional<ProjectGroup> getProjectGroupByProjectIdAndStudentId(Long projectId, Long studentId);

    @Query("""
            select pg.id               as projectGroupId,
                   hofe.studentId      as studentId,
                   hofe.studentName    as fullName,
                   hofe.animalName     as animalName,
                   hofe.evolutionStage as evolutionStage,
                   hofe.groupName      as group,
                   hofe.imageUrl       as imageUrl,
                   cg.grade.id         as gradeId,
                   sum(cg.xp)          as gainedXp,
                   hofe.animalId       as animalId
            from ProjectGroup pg
                     join pg.animals a
                     join HallOfFameEntry hofe on a.id = hofe.animalId
                     join pg.project.criteria c
                     left join CriterionGrade   cg
                               on c.id = cg.criterion.id and cg.grade.gradableEvent.id = :projectId and
                                  cg.grade.animal.id = a.id
            where pg.project.id = :projectId
              and (pg.teachingRoleUser.userId = :teachingRoleUserId or
                   pg.project.eventSection.course.coordinator.userId = :teachingRoleUserId and
                   :showAllProjectGroupsInCourse = true)
            group by pg.id, hofe.studentId, hofe.studentName, hofe.animalName, hofe.evolutionStage,
                     hofe.groupName, hofe.imageUrl, cg.grade.id, hofe.animalId
            
            """)
    List<ProjectTargetDataView> getProjectTargetsData(Long projectId, Long teachingRoleUserId, Boolean showAllProjectGroupsInCourse);

    @Query("""
            select scga.student.userId from ProjectGroup pg
            join pg.animals a
            join a.studentCourseGroupAssignment scga
            where pg.id = :projectGroupId
    """)
    List<Long> getStudentIdsByProjectGroupId(Long projectGroupId);

    @Query("""
            select scga.student from ProjectGroup pg
            join pg.animals a
            join a.studentCourseGroupAssignment scga
            where pg = :projectGroup
    """)
    List<Student> getStudentsByProjectGroup(ProjectGroup projectGroup);

    @Query("""
        select new com.agh.polymorphia_backend.dto.response.user_context.StudentDetailsResponseDto(
            hof.studentId,
            hof.studentName,
            hof.courseId,
            hof.imageUrl,
            hof.animalName,
            hof.evolutionStage,
            hof.groupName,
            hof.position
        )
        from HallOfFameEntry hof
            where hof.animalId in (
                select a.id
                from ProjectGroup pg
                join pg.animals a
                where pg.id = :projectGroupId
            )
    """)
    List<StudentDetailsResponseDto> getUserDetailsResponseByProjectGroupId(Long projectGroupId);

    @Query("""
        select c.id
        from ProjectGroup pg
        join pg.project.eventSection.course c
        where pg.id = :projectGroupId
    """)
    Long getCourseIdByProjectGroupId(Long projectGroupId);

    @Query("""
        select count(a) > 0
        from ProjectGroup pg
        join pg.animals a
        where pg.id = :projectGroupId and a.studentCourseGroupAssignment.student.userId = :studentId
    """)
    boolean isStudentInProjectGroup(Long projectGroupId, Long studentId);

    @Query("""
        select count(pg) > 0
        from ProjectGroup pg
        where pg.id = :projectGroupId and pg.teachingRoleUser.userId = :userId
    """)
    boolean isTeachingRoleUserInProjectGroup(Long projectGroupId, Long userId);


    @Modifying
    @Query(value = """
        delete from project_groups pg
        where pg.id in (
            select distinct pga.project_group_id from project_groups_animals pga
            join students_course_groups scg on scg.animal_id = pga.animal_id
            where scg.course_group_id = :courseGroupId
        )
    """, nativeQuery = true)
    void deleteProjectGroupsByCourseGroupId(Long courseGroupId);
}
