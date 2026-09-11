package com.agh.polymorphia_backend.repository.project;

import com.agh.polymorphia_backend.model.project.ProjectVariantCategory;
import com.agh.polymorphia_backend.repository.project.projection.ProjectVariantCategoryDetailsProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectVariantCategoryRepository extends JpaRepository<ProjectVariantCategory, Long> {

    @Query(value = """
            select pv.id as id,
                   pv.name as name,
                   pv.key as key,
                   pv.project_id as projectId
            from project_variant_categories pv
                join projects p on p.id = pv.project_id
                join gradable_events g on g.id = p.id
                join event_sections e on e.id = g.event_section_id
            where e.course_id = :courseId
            """, nativeQuery = true
    )
    List<ProjectVariantCategoryDetailsProjection> findAllByCourseId(Long courseId);

    @Query("""
                SELECT pvc FROM ProjectVariantCategory pvc
                WHERE pvc.key IN :keys
                AND pvc.project.eventSection.course.id = :courseId
            """)
    List<ProjectVariantCategory> findAllByKeyIn(List<String> keys, Long courseId);

    @Query("""
            select p.id
            from ProjectVariantCategory p
            where p.key=:key and p.project.eventSection.course.id = :courseId
            """)
    Long findIdByKeyAndCourseId(String key, Long courseId);
}
