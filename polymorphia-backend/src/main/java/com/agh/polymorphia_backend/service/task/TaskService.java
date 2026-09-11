package com.agh.polymorphia_backend.service.task;

import com.agh.polymorphia_backend.dto.response.task.TaskAllowedLanguageDto;
import com.agh.polymorphia_backend.dto.response.task.TaskDetailsResponseDto;
import com.agh.polymorphia_backend.dto.response.task.TaskTestCaseDto;
import com.agh.polymorphia_backend.model.gradable_event.subtypes.task.Task;
import com.agh.polymorphia_backend.model.gradable_event.subtypes.task.TaskSupportedLanguage;
import com.agh.polymorphia_backend.repository.task.TaskAllowedLanguageRepository;
import com.agh.polymorphia_backend.repository.task.TaskRepository;
import com.agh.polymorphia_backend.repository.task.TaskTestCaseRepository;
import com.agh.polymorphia_backend.service.validation.TaskAuthorizer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskAllowedLanguageRepository taskAllowedLanguageRepository;
    private final TaskTestCaseRepository taskTestCaseRepository;
    private final TaskAuthorizer taskAuthorizer;

    @Transactional(readOnly = true)
    public Task getTask(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nie znaleziono zadania."));
    }

    @Transactional(readOnly = true)
    public TaskDetailsResponseDto getTaskDetails(Long taskId) {
        taskAuthorizer.checkTaskAccess(taskId);

        getTask(taskId);

        List<TaskAllowedLanguageDto> allowedLanguages =
                taskAllowedLanguageRepository.findAllowedLanguagesWithDetailsByTaskId(taskId).stream()
                        .map(projection -> TaskAllowedLanguageDto.builder()
                                .taskLanguage(projection.getLanguage())
                                .isDefault(projection.getIsDefault())
                                .sampleCode(projection.getSampleCode())
                                .build())
                        .collect(Collectors.toList());

        List<TaskTestCaseDto> testCases = taskTestCaseRepository.findVisibleByTaskId(taskId).stream()
                .map(testCase -> TaskTestCaseDto.builder()
                        .name(testCase.getName())
                        .orderIndex(testCase.getOrderIndex())
                        .input(testCase.getInput())
                        .expectedOutput(testCase.getExpectedOutput())
                        .build())
                        .collect(Collectors.toList());

        return TaskDetailsResponseDto.builder()
                .allowedLanguages(allowedLanguages)
                .testCases(testCases)
                .build();
    }

    public void validateTaskLanguage(Long taskId, TaskSupportedLanguage language) {
        boolean languageAllowed = taskAllowedLanguageRepository.existsByTaskIdAndLanguage(taskId, language);

        if (!languageAllowed) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Zadanie nie może być uruchomione w tym języku.");
        }
    }
}
