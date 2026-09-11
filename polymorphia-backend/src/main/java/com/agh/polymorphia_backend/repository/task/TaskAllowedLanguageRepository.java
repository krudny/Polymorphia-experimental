package com.agh.polymorphia_backend.repository.task;

import com.agh.polymorphia_backend.model.gradable_event.subtypes.task.TaskAllowedLanguage;
import com.agh.polymorphia_backend.model.gradable_event.subtypes.task.TaskSupportedLanguage;
import com.agh.polymorphia_backend.repository.task.projections.TaskAllowedLanguageProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TaskAllowedLanguageRepository extends JpaRepository<TaskAllowedLanguage, Long> {
    @Query("""
            SELECT
                taskAllowedLanguage.language AS language,
                (CASE WHEN taskAllowedLanguage.language = task.defaultLanguage THEN true ELSE false END) AS isDefault,
                COALESCE(taskStarterCode.starterCode, '') AS sampleCode
            FROM TaskAllowedLanguage taskAllowedLanguage
            JOIN taskAllowedLanguage.task task
            LEFT JOIN TaskStarterCode taskStarterCode ON taskStarterCode.task.id = taskAllowedLanguage.task.id AND taskStarterCode.language = taskAllowedLanguage.language
            WHERE taskAllowedLanguage.task.id = :taskId
            """)
    List<TaskAllowedLanguageProjection> findAllowedLanguagesWithDetailsByTaskId(@Param("taskId") Long taskId);

    boolean existsByTaskIdAndLanguage(@Param("taskId") Long taskId, @Param("language") TaskSupportedLanguage supportedLanguage);
}

