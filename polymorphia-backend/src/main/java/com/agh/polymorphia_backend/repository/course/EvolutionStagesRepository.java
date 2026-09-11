package com.agh.polymorphia_backend.repository.course;

import com.agh.polymorphia_backend.model.user.student.EvolutionStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvolutionStagesRepository extends JpaRepository<EvolutionStage, Long> {
    @Query("""
            select es
            from EvolutionStage es
            where es.course.id = :courseId
            order by es.orderIndex
            """)
    List<EvolutionStage> findAllByCourseId(Long courseId);

    @Query("""
                SELECT e FROM EvolutionStage e
                WHERE e.key IN :keys
                AND e.course.id = :courseId
            """)
    List<EvolutionStage> findAllByKeyIn(List<String> keys, Long courseId);
}
