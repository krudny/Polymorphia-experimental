package com.agh.polymorphia_backend.repository.user.role;

import com.agh.polymorphia_backend.model.user.student.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    @Query("""
            select count(scga) > 0
            from StudentCourseGroupAssignment scga
            where scga.student.userId=:userId and scga.courseGroup.course.id=:courseId
    """)
    boolean isUserStudentAssignedToCourseGroup(Long userId, Long courseId);

    Optional<Student> findByIndexNumber(Integer indexNumber);

    Optional<Student> findByUserId(Long userId);

    @Query(
            """
                        SELECT s FROM GradableEvent ge
                            JOIN ge.eventSection es
                            JOIN es.course c
                            JOIN c.courseGroups cg
                            JOIN cg.studentCourseGroupAssignments scga
                            JOIN scga.student s
                            WHERE s.userId = :userId AND ge.id = :gradableEventId AND cg.teachingRoleUser.userId = :teachingRoleUserId
                    """
    )
    Optional<Student> findByUserIdAndGradableEventIdAndCourseGroupTeachingRoleUserId(
            @Param("userId") Long userId,
            @Param("gradableEventId") Long gradableEventId,
            @Param("teachingRoleUserId") Long teachingRoleUserId
    );

    @Query(
            """
                        SELECT s FROM GradableEvent ge
                            JOIN ge.eventSection es
                            JOIN es.course c
                            JOIN c.courseGroups cg
                            JOIN cg.studentCourseGroupAssignments scga
                            JOIN scga.student s
                            WHERE s.userId = :userId AND ge.id = :gradableEventId
                    """
    )
    Optional<Student> findByUserIdAndGradableEventId(
            @Param("userId") Long userId,
            @Param("gradableEventId") Long gradableEventId
    );
}
