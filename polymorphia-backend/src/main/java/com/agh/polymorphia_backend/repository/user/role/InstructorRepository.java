package com.agh.polymorphia_backend.repository.user.role;

import com.agh.polymorphia_backend.model.user.instructor.Instructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InstructorRepository extends JpaRepository<Instructor, Long> {
    @Query("""
            select count(tru) > 0
            from CourseGroup cg
            join cg.teachingRoleUser tru
            where tru.user.id=:teachingRoleUserId and cg.course.id=:courseId
    """)
    boolean isUserTeachingRoleUserInCourse(Long teachingRoleUserId, Long courseId);

    Optional<Instructor> findByUserId(Long userId);

    @Query("""
            select count(tru) > 0
            from StudentCourseGroupAssignment scg
            join scg.courseGroup cg
            join cg.teachingRoleUser tru
            where scg.student.user.id=:studentId
                and cg.course.id=:courseId
                and tru.user.id=:teachingRoleUserId
            """)
    boolean hasAccessToStudentInCourse(Long teachingRoleUserId, Long courseId, Long studentId);
}