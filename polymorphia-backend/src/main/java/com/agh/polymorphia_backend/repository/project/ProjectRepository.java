package com.agh.polymorphia_backend.repository.project;

import com.agh.polymorphia_backend.dto.response.project.ProjectGroupPickStudentsResponseDto;
import com.agh.polymorphia_backend.model.project.ProjectGroup;
import com.agh.polymorphia_backend.model.project.ProjectVariantCategory;
import com.agh.polymorphia_backend.model.gradable_event.subtypes.project.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    @Query("""                                                                                                                                                                                                                                                                                                                                                                                                                      
      select distinct c
      from ProjectVariantCategory c
      left join fetch c.variants v
      where c.project.id = :projectId
      order by c.id, v.id
  """)
    List<ProjectVariantCategory> findCategoriesWithVariants(Long projectId);

    @Query("""
            select hofe.studentId      as id,
                   hofe.studentName    as fullName,
                   hofe.animalName     as animalName,
                   hofe.evolutionStage as evolutionStage,
                   hofe.groupName      as group,
                   hofe.imageUrl       as imageUrl,
                   hofe.position       as position
            from HallOfFameEntry hofe
                     join StudentCourseGroupAssignment scga on scga.animal.id = hofe.animalId
            where (:#{#project.isAllowCrossCourseGroupProjectGroups()} = true or
                   scga.courseGroup.teachingRoleUser.userId = :teachingRoleUserId or
                   scga.courseGroup.course.coordinator.userId = :teachingRoleUserId)
              and (:includeAllGroups = true or hofe.groupName in :groups)
              and hofe.courseId = :courseId
              and not exists (
                         select 1
                         from ProjectGroup pg
                              join pg.animals a
                              join pg.project p
                         where a.id = hofe.animalId
                           and p.id = :#{#project.id}
                   )
            order by hofe.studentName
            """)
    List<ProjectGroupPickStudentsResponseDto> getProjectGroupConfigurationGroupPickStudents(
            @Param("courseId") Long courseId,
            @Param("project") Project project,
            @Param("teachingRoleUserId") Long teachingRoleUserId,
            @Param("groups") List<String> groups,
            @Param("includeAllGroups") Boolean includeAllGroups
    );

    @Query("""
            select hofe.studentId      as id,
                   hofe.studentName    as fullName,
                   hofe.animalName     as animalName,
                   hofe.evolutionStage as evolutionStage,
                   hofe.groupName      as group,
                   hofe.imageUrl       as imageUrl,
                   hofe.position       as position
            from HallOfFameEntry hofe
            where hofe.animalId in (
                         select a.id
                         from ProjectGroup pg
                              join pg.animals a
                         where pg = :projectGroup
                   )
            order by hofe.studentName
            """)
    List<ProjectGroupPickStudentsResponseDto> getProjectGroupConfigurationGroupPickStudents(
            @Param("projectGroup") ProjectGroup projectGroup
    );

    @Query("""
        select p.eventSection.course.id
        from Project p
        where p.id = :projectId
    """)
    Long getCourseIdByProjectId(Long projectId);

    @Query(value = """
        WITH RECURSIVE
        groups AS (
            SELECT pg.id AS group_id
            FROM project_groups pg
            WHERE pg.project_id = :projectId
        ),

        variant_stats AS (
            SELECT
                pv.id  AS variant_id,
                pvc.id AS category_id,
                COALESCE(COUNT(DISTINCT g.group_id), 0) AS used,
                ROW_NUMBER() OVER (
                    PARTITION BY pvc.id
                    ORDER BY COALESCE(COUNT(DISTINCT g.group_id), 0), random()
                ) AS rn_in_category
            FROM project_variant_categories pvc
            JOIN project_variants pv
              ON pv.project_variant_category_id = pvc.id
            LEFT JOIN project_groups_project_variants pgpv
              ON pgpv.project_variant_id = pv.id
            LEFT JOIN groups g
              ON g.group_id = pgpv.project_group_id
            WHERE pvc.project_id = :projectId
            GROUP BY pv.id, pvc.id
        ),

        best_variants AS (
            SELECT *
            FROM variant_stats
            WHERE rn_in_category <= :perCategoryLimit
        ),

        categories AS (
            SELECT DISTINCT category_id
            FROM best_variants
        ),
        ordered_categories AS (
            SELECT
                category_id,
                ROW_NUMBER() OVER (ORDER BY category_id) AS pos,
                COUNT(*) OVER () AS max_pos
            FROM categories
        ),

        combos AS (
            SELECT
                oc.pos,
                oc.max_pos,
                ARRAY[bv.variant_id] AS variant_ids,
                bv.used              AS sum_used
            FROM ordered_categories oc
            JOIN best_variants bv
              ON bv.category_id = oc.category_id
            WHERE oc.pos = 1

            UNION ALL

            SELECT
                oc.pos,
                oc.max_pos,
                c.variant_ids || bv.variant_id,
                c.sum_used + bv.used
            FROM combos c
            JOIN ordered_categories oc
              ON oc.pos = c.pos + 1
            JOIN best_variants bv
              ON bv.category_id = oc.category_id
        ),

        final_combos AS (
            SELECT
                variant_ids,
                sum_used
            FROM combos
            WHERE pos = max_pos
        ),

        existing_tuples AS (
            SELECT
                tuple_variants,
                COUNT(*) AS tuple_used
            FROM (
                SELECT
                    g.group_id,
                    ARRAY_AGG(pgpv.project_variant_id ORDER BY oc.pos) AS tuple_variants
                FROM groups g
                JOIN project_groups_project_variants pgpv
                  ON pgpv.project_group_id = g.group_id
                JOIN project_variants pv
                  ON pv.id = pgpv.project_variant_id
                JOIN ordered_categories oc
                  ON oc.category_id = pv.project_variant_category_id
                WHERE (:groupId IS NULL OR g.group_id <> :groupId)
                GROUP BY g.group_id
                HAVING COUNT(DISTINCT oc.category_id) =
                       (SELECT max_pos FROM ordered_categories LIMIT 1)
            ) s
            GROUP BY tuple_variants
        ),

        scored_combos AS (
            SELECT
                fc.variant_ids,
                fc.sum_used,
                COALESCE(et.tuple_used, 0) AS tuple_used
            FROM final_combos fc
            LEFT JOIN existing_tuples et
              ON et.tuple_variants = fc.variant_ids
        ),

        best_combo AS (
            SELECT *
            FROM scored_combos
            ORDER BY
                tuple_used,
                sum_used,
                random()
            LIMIT 1
        )

        SELECT
            oc.category_id AS categoryId,
            bc.variant_ids[oc.pos] AS variantId
        FROM best_combo bc
        JOIN ordered_categories oc ON TRUE
        ORDER BY oc.category_id
        """,
            nativeQuery = true)
    List<SuggestedVariant> suggestVariantsForNewGroup(
            @Param("projectId") Long projectId,
            @Param("groupId") Long groupId,
            @Param("perCategoryLimit") int perCategoryLimit
    );
}
