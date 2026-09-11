package com.agh.polymorphia_backend.repository.reward;

import com.agh.polymorphia_backend.model.reward.Item;
import com.agh.polymorphia_backend.repository.reward.projection.ItemDetailsDetailsProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findAllByCourseId(Long courseId);

    @Query("""
                SELECT i FROM Item i
                WHERE i.course.id = :courseId
                  AND (:excludedItemIds is null OR i.id NOT IN :excludedItemIds)
            """)
    List<Item> findAllByCourseIdAndItemIdNotIn(Long courseId, List<Long> excludedItemIds);

    @Query(value = """
            SELECT
                r.id,
                r.key,
                r.name,
                r.description,
                r.image_url,
                i.limit,
                es.key as event_section_key,
                CASE
                    WHEN fbi.item_id IS NOT NULL THEN 'FLAT_BONUS'
                    WHEN pbi.item_id IS NOT NULL THEN 'PERCENTAGE_BONUS'
                END as item_type,
                fbi.xp_bonus,
                fbi.behavior,
                pbi.percentage_bonus
            FROM items i
            INNER JOIN rewards r ON i.reward_id = r.id
            LEFT JOIN event_sections es ON i.event_section_id = es.id
            LEFT JOIN flat_bonus_items fbi ON i.reward_id = fbi.item_id
            LEFT JOIN percentage_bonus_items pbi ON i.reward_id = pbi.item_id
            WHERE r.course_id = :courseId
            ORDER BY r.order_index
            """, nativeQuery = true)
    List<ItemDetailsDetailsProjection> findAllItemDetailsByCourseId(@Param("courseId") Long courseId);

    @Query("""
                SELECT i FROM Item i
                WHERE i.key IN :keys
                AND i.course.id = :courseId
            """)
    List<Item> findAllByKeyIn(List<String> keys, Long courseId);
}