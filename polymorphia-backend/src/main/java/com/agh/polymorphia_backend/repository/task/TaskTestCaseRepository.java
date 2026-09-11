package com.agh.polymorphia_backend.repository.task;

import com.agh.polymorphia_backend.model.gradable_event.subtypes.task.TaskTestCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TaskTestCaseRepository extends JpaRepository<TaskTestCase, Long> {
    @Query("""
            SELECT tc
            FROM TaskTestCase tc
            WHERE tc.task.id = :taskId
            ORDER BY tc.orderIndex ASC
            """)
    List<TaskTestCase> findByTaskId(@Param("taskId") Long taskId);

    @Query("""
            SELECT tc
            FROM TaskTestCase tc
            WHERE tc.task.id = :taskId
              AND tc.isVisible = true
            ORDER BY tc.orderIndex ASC
            """)
    List<TaskTestCase> findVisibleByTaskId(@Param("taskId") Long taskId);

    @Query("""
            SELECT tc
            FROM TaskTestCase tc
            WHERE tc.task.id IN :taskIds
            ORDER BY tc.orderIndex ASC
            """)
    List<TaskTestCase> findByTaskIdIn(@Param("taskIds") List<Long> taskIds);
}
