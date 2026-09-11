package com.agh.polymorphia_backend.service.task.async_submission;

import com.agh.polymorphia_backend.service.task.dto.TaskSubmissionContext;
import com.agh.polymorphia_backend.service.task.dto.TaskTestCaseOutcome;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskSubmissionProcessor {

  private final TaskSubmissionExecutionService taskSubmissionExecutionService;
  private final TaskSubmissionPersistenceService taskSubmissionPersistenceService;
  private final TaskSubmissionGradingService taskSubmissionGradingService;

  public void process(TaskSubmissionContext taskSubmissionContext) {
      boolean processedSuccessfully = executeAndSave(taskSubmissionContext);

      if (processedSuccessfully) {
          grade(taskSubmissionContext.taskSubmissionId());
      }
  }

  public void grade(Long taskSubmissionId) {
    try {
      taskSubmissionGradingService.applyGrade(taskSubmissionId);
      taskSubmissionPersistenceService.markGraded(taskSubmissionId);
    } catch (Exception exception) {
      log.error("Failed to grade task submission {}, reaper will retry", taskSubmissionId, exception);
    }
  }

  private boolean executeAndSave(TaskSubmissionContext taskSubmissionContext) {
    try {
      List<TaskTestCaseOutcome> outcomes = taskSubmissionExecutionService.run(taskSubmissionContext);
      taskSubmissionPersistenceService.saveResultsAndComplete(taskSubmissionContext, outcomes);
      return true;
    } catch (Exception exception) {
      log.error("Failed to run task submission {}", taskSubmissionContext.taskSubmissionId(), exception);
      taskSubmissionPersistenceService.markFailed(taskSubmissionContext.taskSubmissionId(), taskSubmissionContext.leaseToken());
      return false;
    }
  }
}