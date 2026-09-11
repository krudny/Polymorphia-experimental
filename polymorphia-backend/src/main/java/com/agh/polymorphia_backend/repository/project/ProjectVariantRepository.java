package com.agh.polymorphia_backend.repository.project;

import com.agh.polymorphia_backend.model.project.ProjectVariant;
import com.agh.polymorphia_backend.repository.project.projection.ProjectVariantDetailsProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectVariantRepository extends JpaRepository<ProjectVariant, Long> {
    @Query(value = """
            select pv.project_variant_category_id as variant_category_id,
                   pv.name as name,
                   pv.key as key,
                   pv.description as description,
                   pv.image_url as image_url,
                   pv.short_code as short_code
            from project_variants pv
                join project_variant_categories pvc on pvc.id = pv.project_variant_category_id
                join projects p on p.id = pvc.project_id
                join gradable_events g on g.id = p.id
                join event_sections e on e.id = g.event_section_id
            where e.course_id = :courseId
            """, nativeQuery = true
    )
    List<ProjectVariantDetailsProjection> findAllByCourseId(Long courseId);

    @Query("""
                SELECT p from ProjectVariant p
                WHERE p.key IN :keys
                AND p.category.project.eventSection.course.id = :courseId
            """)
    List<ProjectVariant> findAllByKeyIn(List<String> keys, Long courseId);
}
