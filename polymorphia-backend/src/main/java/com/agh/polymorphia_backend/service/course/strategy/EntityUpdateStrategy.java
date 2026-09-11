package com.agh.polymorphia_backend.service.course.strategy;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public interface EntityUpdateStrategy<Dto, Entity> {
    /**
     * @return a function that extracts the key from the dto
     */
    Function<Dto, String> getKeyExtractor();

    /**
     * @return a function that extracts the key from the entity
     */
    Function<Entity, String> getEntityKeyExtractor();

    /**
     * @return a function that extracts type for entities with subclasses
     * it is needed so that on type change (when course is updated)
     * we could delete the old entity that was of the old type
     * and create a new one with the new type
     */
    Function<Dto, ?> getTypeExtractor();

    /**
     * @return entity's repository
     */
    JpaRepository<Entity, Long> getRepository();

    /**
     * For every key finds matching item within course with courseId
     *
     * @param keys     list of entity keys to search for
     * @param courseId the course ID to filter by
     * @return list of entities matching the keys and course ID
     */
    List<Entity> findAllByKeys(List<String> keys, Long courseId);

    /**
     * @param dto with requested changes for the entity
     * @return changed entity
     * When the entity is created, we create it under proper subclass
     */
    Entity createNewEntity(Dto dto);

    /**
     * Updates an existing entity with data from the DTO.
     *
     * @param entity         the entity to update
     * @param dto            the DTO containing updated values
     * @param orderIds       map of DTOs to their orderIds
     * @param parentEntityId the id of the parent entity
     * @return the updated entity
     */
    Entity updateEntity(Entity entity, Dto dto, Map<Dto, Long> orderIds, Long parentEntityId);

    /**
     * Flushes pending changes to the database.
     */
    default void flush() {
        getRepository().flush();
    }
}