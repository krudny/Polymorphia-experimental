package com.agh.polymorphia_backend.repository.reward;

import com.agh.polymorphia_backend.model.reward.Chest;
import com.agh.polymorphia_backend.repository.reward.projection.ChestDetailsDetailsProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChestRepository extends JpaRepository<Chest, Long> {
    List<Chest> findAllByCourseId(Long courseId);

    @Query("""
                SELECT c FROM Chest c
                WHERE c.course.id = :courseId
                  AND (:excludedChestIds is null OR c.id NOT IN :excludedChestIds)
            """)
    List<Chest> findAllByCourseIdAndChestIdNotIn(Long courseId, List<Long> excludedChestIds);


    @Query(value = """
                        SELECT
                            c.key AS key,
                            c.name AS name,
                            c.description AS description,
                            c.image_url AS imageUrl,
                            ch.behavior AS behavior,
                            array_remove(array_agg(i.key ORDER BY ci.item_id), NULL) AS itemKeys
                        FROM chests ch
                        JOIN rewards c ON ch.reward_id = c.id
                        LEFT JOIN chests_items ci ON ch.reward_id = ci.chest_id
                        LEFT JOIN items it ON ci.item_id = it.reward_id
                        LEFT JOIN rewards i ON it.reward_id = i.id
                        WHERE c.course_id = :courseId
                        GROUP BY c.id, c.key, c.name, c.description, c.image_url, ch.behavior, c.order_index
                        ORDER BY c.order_index
            """, nativeQuery = true)
    List<ChestDetailsDetailsProjection> findAllChestDetailsByCourseId(@Param("courseId") Long courseId);

    @Query("""
                SELECT c FROM Chest c
                WHERE c.key IN :keys
                AND c.course.id = :courseId
            """)
    List<Chest> findAllByKeyIn(List<String> keys, Long courseId);
}
