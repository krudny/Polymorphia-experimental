package com.agh.polymorphia_backend.service.task;

import com.agh.polymorphia_backend.dto.request.task.ExecuteTaskRequestDto;
import com.agh.polymorphia_backend.dto.response.task.SubmitTaskResponseDto;
import com.agh.polymorphia_backend.dto.response.task.TaskSubmissionStatusResponseDto;
import com.agh.polymorphia_backend.dto.response.task.TestCaseResultDto;
import com.agh.polymorphia_backend.model.gradable_event.subtypes.task.Task;
import com.agh.polymorphia_backend.model.gradable_event.subtypes.task.TaskSubmission;
import com.agh.polymorphia_backend.model.gradable_event.subtypes.task.TaskSubmissionResult;
import com.agh.polymorphia_backend.model.gradable_event.subtypes.task.TaskSubmissionStatus;
import com.agh.polymorphia_backend.model.user.AbstractRoleUser;
import com.agh.polymorphia_backend.model.user.student.Animal;
import com.agh.polymorphia_backend.repository.task.TaskSubmissionRepository;
import com.agh.polymorphia_backend.repository.task.TaskSubmissionResultRepository;
import com.agh.polymorphia_backend.service.gradable_event.GradableEventService;
import com.agh.polymorphia_backend.service.mapper.TaskSubmissionResultMapper;
import com.agh.polymorphia_backend.service.student.AnimalService;
import com.agh.polymorphia_backend.service.user.UserService;
import com.agh.polymorphia_backend.service.validation.TaskAuthorizer;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskSubmissionService {

    private final TaskService taskService;
    private final TaskSubmissionRepository taskSubmissionRepository;
    private final TaskSubmissionResultRepository taskSubmissionResultRepository;
    private final TaskSubmissionResultMapper taskSubmissionResultMapper;
    private final TaskAuthorizer taskAuthorizer;
    private final UserService userService;
    private final AnimalService animalService;
    private final GradableEventService gradableEventService;

    @Transactional
    public SubmitTaskResponseDto submitTask(Long taskId, ExecuteTaskRequestDto request) {
        taskAuthorizer.checkTaskAccess(taskId);
        Task task = taskService.getTask(taskId);
        taskService.validateTaskLanguage(taskId, request.getTaskLanguage());

        Animal animal = resolveCurrentUserAnimal(taskId);
        int userAttempt = resolveNextUserAttempt(taskId, animal.getId());

        TaskSubmission taskSubmission = TaskSubmission.builder()
                .task(task)
                .animal(animal)
                .language(request.getTaskLanguage())
                .sourceCode(request.getSourceCode())
                .status(TaskSubmissionStatus.QUEUED)
                .userAttempt(userAttempt)
                .build();

        TaskSubmission savedTaskSubmission;
        try {
            savedTaskSubmission = taskSubmissionRepository.save(taskSubmission);
            taskSubmissionRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Zgłoszenie jest już przetwarzane. Spróbuj ponownie.");
        }

        return SubmitTaskResponseDto.builder()
                .submissionId(savedTaskSubmission.getId())
                .status(savedTaskSubmission.getStatus())
                .build();
    }

    @Transactional(readOnly = true)
    public TaskSubmissionStatusResponseDto getTaskStatus(Long taskId, Long taskSubmissionId) {
        TaskSubmission taskSubmission = taskSubmissionRepository.findById(taskSubmissionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nie znaleziono zgłoszenia."));

        if (!taskSubmission.getTask().getId().equals(taskId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Nie znaleziono zgłoszenia.");
        }

        taskAuthorizer.checkTaskSubmissionAccess(taskSubmission);

        List<TaskSubmissionResult> visibleResults =
                taskSubmissionResultRepository.findVisibleResultsBySubmissionId(taskSubmissionId);
        List<TestCaseResultDto> visibleResult = visibleResults.stream()
                .map(taskSubmissionResultMapper::toDto)
                .toList();

        Integer totalExecutionTimeMs =
                taskSubmissionResultRepository.sumExecutionTimeMsBySubmissionId(taskSubmissionId);

        return TaskSubmissionStatusResponseDto.builder()
                .submissionId(taskSubmission.getId())
                .status(taskSubmission.getStatus())
                .score(taskSubmission.getScore())
                .passedCount(taskSubmission.getPassedCount())
                .totalCount(taskSubmission.getTotalCount())
                .totalExecutionTimeMs(totalExecutionTimeMs > 0 ? totalExecutionTimeMs : null)
                .createdDate(taskSubmission.getCreatedDate())
                .visibleResults(visibleResult)
                .errorMessage(taskSubmission.getErrorMessage())
                .build();
    }

    private Animal resolveCurrentUserAnimal(Long taskId) {
        Long courseId = gradableEventService.getCourseIdByGradableEventId(taskId);
        AbstractRoleUser currentUser = userService.getCurrentUser();
        return animalService.getAnimal(currentUser.getUserId(), courseId);
    }

    private int resolveNextUserAttempt(Long taskId, Long animalId) {
        return taskSubmissionRepository.countByTaskIdAndAnimalId(taskId, animalId) + 1;
    }
}
