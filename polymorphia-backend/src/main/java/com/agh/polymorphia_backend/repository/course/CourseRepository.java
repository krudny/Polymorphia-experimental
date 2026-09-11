package com.agh.polymorphia_backend.repository.course;

import com.agh.polymorphia_backend.model.course.Course;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    @Query("""
        select count(c) > 0
        from Course c
        where c.id = :courseId and c.coordinator.userId = :userId
    """)
    boolean isUserCoordinatorOfCourse(Long userId, Long courseId);

    @EntityGraph(attributePaths = {
            "evolutionStages",
            "eventSections",
            "eventSections.criteria",
            "eventSections.criteria.rewards",
            "eventSections.gradableEvents",
            "eventSections.submissionRequirements",
            "eventSections.variantCategories",
            "eventSections.variantCategories.variants",
            "items",
            "chests",
            "chests.items"
    })
    Optional<Course> findCourseDetailsById(Long id);
}