package com.agh.polymorphia_backend.service.submission;

import com.agh.polymorphia_backend.dto.request.SubmissionDetailsRequestDto;
import com.agh.polymorphia_backend.dto.request.target.StudentGroupTargetRequestDto;
import com.agh.polymorphia_backend.dto.request.target.StudentTargetRequestDto;
import com.agh.polymorphia_backend.dto.request.target.TargetRequestDto;
import com.agh.polymorphia_backend.dto.response.submission.SubmissionDetailsResponseDto;
import com.agh.polymorphia_backend.dto.response.submission.SubmissionRequirementResponseDto;
import com.agh.polymorphia_backend.model.event_section.EventSectionType;
import com.agh.polymorphia_backend.model.gradable_event.GradableEvent;
import com.agh.polymorphia_backend.model.project.ProjectGroup;
import com.agh.polymorphia_backend.model.submission.Submission;
import com.agh.polymorphia_backend.model.submission.SubmissionRequirement;
import com.agh.polymorphia_backend.model.user.UserType;
import com.agh.polymorphia_backend.model.user.student.Student;
import com.agh.polymorphia_backend.repository.project.ProjectGroupRepository;
import com.agh.polymorphia_backend.repository.submission.SubmissionRepository;
import com.agh.polymorphia_backend.repository.submission.SubmissionRequirementRepository;
import com.agh.polymorphia_backend.repository.user.role.StudentRepository;
import com.agh.polymorphia_backend.service.gradable_event.GradableEventService;
import com.agh.polymorphia_backend.service.mapper.SubmissionMapper;
import com.agh.polymorphia_backend.service.student.AnimalService;
import com.agh.polymorphia_backend.service.user.UserService;
import com.agh.polymorphia_backend.service.validation.AccessAuthorizer;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class SubmissionService {
    private static final String STUDENT_NOT_FOUND = "Nie znaleziono studenta.";
    private static final String FORBIDDEN_LOCK_CHANGE = "Studenci nie mogą zmieniać statusu blokady.";
    private static final String INVALID_ROLE = "Użytkownik musi mieć poprawną rolę.";

    private final GradableEventService gradableEventService;
    private final AccessAuthorizer accessAuthorizer;
    private final SubmissionRequirementRepository submissionRequirementRepository;
    private final SubmissionMapper submissionMapper;
    private final UserService userService;
    private final SubmissionRepository submissionRepository;
    private final ProjectGroupRepository projectGroupRepository;
    private final StudentRepository studentRepository;
    private final AnimalService animalService;

    public List<SubmissionRequirementResponseDto> getSubmissionRequirements(Long gradableEventId) {
        GradableEvent gradableEvent = validateUsageAndGetGradableEvent(gradableEventId);

        return submissionRequirementRepository.getSubmissionRequirementsByGradableEvent(gradableEvent).stream()
                .sorted(Comparator.comparing(SubmissionRequirement::getOrderIndex))
                .map(submissionMapper::toSubmissionRequirementResponseDto).toList();
    }

    public SubmissionDetailsResponseDto getSubmissionDetails(Long gradableEventId, TargetRequestDto target) {
        GradableEvent gradableEvent = validateUsageAndGetGradableEventForTarget(gradableEventId, target);

        List<Student> submissionSources = getSubmissionSourcesForTarget(gradableEvent, target);

        List<SubmissionRequirement> submissionRequirements =
                submissionRequirementRepository.getSubmissionRequirementsByGradableEvent(gradableEvent);
        List<Submission> submissions = submissionRepository.getSubmissionsByGradableEventAndStudent(gradableEventId,
                submissionSources.getFirst().getUserId());

        return submissionMapper.toSubmissionDetailsResponseDto(submissions, submissionRequirements);
    }

    @Transactional
    public void putSubmissionDetails(Long gradableEventId, SubmissionDetailsRequestDto requestDto) {
        GradableEvent gradableEvent = validateUsageAndGetGradableEventForTarget(gradableEventId, requestDto.target());

        List<Student> submissionSources = getSubmissionSourcesForTarget(gradableEvent, requestDto.target());

        Map<Long, SubmissionRequirement> submissionRequirements =
                submissionRequirementRepository.getSubmissionRequirementsByGradableEvent(gradableEvent).stream()
                        .collect(Collectors.toMap(SubmissionRequirement::getId, Function.identity()));

        Set<Long> submissionRequirementsIds = submissionRequirements.keySet();
        Set<Long> receivedSubmissionRequirementsIds = requestDto.details().keySet();

        if (submissionRequirementsIds.size() != receivedSubmissionRequirementsIds.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Szczegóły oddania zadania muszą zawierać wszystkie wymagania.");
        }

        // studentId -> (submissionRequirementId -> submission)
        Map<Long, Map<Long, Submission>> submissionsPerStudent = submissionSources.stream().collect(
                Collectors.toMap(Student::getUserId,
                        submissionSource -> submissionRepository.getSubmissionsByGradableEventAndStudent(
                                gradableEventId, submissionSource.getUserId()).stream().collect(
                                Collectors.toMap(submission -> submission.getSubmissionRequirement().getId(),
                                        Function.identity()))));

        List<Submission> submissionsToDelete = new ArrayList<>();
        List<Submission> submissionsToUpdate = new ArrayList<>();

        requestDto.details().forEach((submissionRequirementId, newDetails) -> {
            List<Submission> submissions = new ArrayList<>();
            submissionsPerStudent.forEach((studentId, studentSubmissions) -> submissions.add(
                    studentSubmissions.get(submissionRequirementId)));

            boolean isEmpty = !newDetails.isLocked() && newDetails.url().isEmpty();

            if (userService.getCurrentUserRole() == UserType.STUDENT && isEmpty &&
                    submissionRequirements.get(submissionRequirementId).isMandatory()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Brak obowiązkowego wymagania.");
            }

            Optional.ofNullable(submissions.getFirst()).ifPresentOrElse(currentSubmission -> {
                boolean isLockChanged = currentSubmission.isLocked() != newDetails.isLocked();
                boolean isUrlChanged = !Objects.equals(currentSubmission.getUrl(), newDetails.url());
                boolean isChanged = isLockChanged || isUrlChanged;

                if (!isChanged) {
                    return;
                }

                if (userService.getCurrentUserRole() == UserType.STUDENT) {
                    if (currentSubmission.isLocked()) {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                                "Nie można zmienić zablokowanego zgłoszenia.");
                    }

                    if (isLockChanged) {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN, FORBIDDEN_LOCK_CHANGE);
                    }
                }

                if (isEmpty) {
                    submissionsToDelete.addAll(submissions);
                } else {
                    submissions.forEach(submission -> {
                        submission.setUrl(newDetails.url());
                        submission.setLocked(newDetails.isLocked());
                    });
                    submissionsToUpdate.addAll(submissions);
                }
            }, () -> {
                if (userService.getCurrentUserRole() == UserType.STUDENT && newDetails.isLocked()) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, FORBIDDEN_LOCK_CHANGE);
                }

                if (isEmpty) {
                    return;
                }

                submissionSources.forEach(student -> {
                    Submission submission = Submission.builder()
                            .submissionRequirement(submissionRequirements.get(submissionRequirementId))
                            .animal(animalService.getAnimal(student.getUserId(),
                                    gradableEvent.getEventSection().getCourse().getId())).url(newDetails.url())
                            .isLocked(newDetails.isLocked()).build();
                    submissionsToUpdate.add(submission);
                });
            });
        });

        try {
            submissionRepository.saveAll(submissionsToUpdate);
            submissionRepository.deleteAll(submissionsToDelete);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Nie udało się zapisać zadania.");
        }
    }

    private List<Student> getSubmissionSourcesForTarget(GradableEvent gradableEvent, TargetRequestDto target) {
        Long userId = userService.getCurrentUser().getUserId();
        UserType userType = userService.getCurrentUserRole();
        switch (target) {
            case StudentTargetRequestDto studentTargetRequestDto -> {
                if (userType.equals(UserType.STUDENT) && (!userId.equals(studentTargetRequestDto.id()))) {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, STUDENT_NOT_FOUND);
                }
                switch (gradableEvent.getEventSection().getEventSectionType()) {
                    case ASSIGNMENT -> {
                        return List.of(
                                getStudentListForAssigment(gradableEvent.getId(), studentTargetRequestDto.id(), userId,
                                        userType).orElseThrow(
                                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, STUDENT_NOT_FOUND)));
                    }
                    case PROJECT -> {
                        return getStudentListFromProjectGroup(
                                getProjectGroupForStudentTarget(gradableEvent.getId(), studentTargetRequestDto.id(),
                                        userId, userType));
                    }
                    default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Tylko wydarzenia z sekcji zadaniowej i projektowej wspierają oddawanie zadania.");
                }
            }
            case StudentGroupTargetRequestDto studentGroupTargetRequestDto -> {
                return getStudentListFromProjectGroup(getProjectGroupForStudentGroupTarget(gradableEvent.getId(),
                        studentGroupTargetRequestDto.groupId(), userId, userType));
            }
        }
    }

    private Optional<Student> getStudentListForAssigment(Long gradableEventId, Long studentId, Long userId,
                                                         UserType userType) {
        return switch (userType) {
            case STUDENT -> Optional.of((Student) userService.getCurrentUser());
            case INSTRUCTOR ->
                    studentRepository.findByUserIdAndGradableEventIdAndCourseGroupTeachingRoleUserId(studentId,
                            gradableEventId, userId);
            case COORDINATOR -> studentRepository.findByUserIdAndGradableEventId(studentId, gradableEventId);
            case UNDEFINED -> throw new ResponseStatusException(HttpStatus.FORBIDDEN, INVALID_ROLE);
        };
    }

    private Optional<ProjectGroup> getProjectGroupForStudentTarget(Long projectId, Long studentId, Long userId,
                                                                   UserType userType) {
        return switch (userType) {
            case STUDENT -> projectGroupRepository.getProjectGroupByStudentIdAndProjectId(userId, projectId);
            case INSTRUCTOR ->
                    projectGroupRepository.getProjectGroupByStudentIdAndProjectIdAndTeachingRoleUserId(studentId, projectId,
                            userId);
            case COORDINATOR -> projectGroupRepository.getProjectGroupByStudentIdAndProjectId(studentId, projectId);
            case UNDEFINED -> throw new ResponseStatusException(HttpStatus.FORBIDDEN, INVALID_ROLE);
        };
    }

    private Optional<ProjectGroup> getProjectGroupForStudentGroupTarget(Long gradableEventId, Long groupId, Long userId,
                                                                        UserType userType) {
        return switch (userType) {
            case STUDENT -> projectGroupRepository.getProjectGroupByIdAndStudentIdAndProjectId(groupId, userId,
                    gradableEventId);
            case INSTRUCTOR ->
                    projectGroupRepository.getProjectGroupByIdAndProjectIdAndTeachingRoleUserId(groupId, gradableEventId,
                            userId);
            case COORDINATOR -> projectGroupRepository.getProjectGroupByIdAndProjectId(groupId, gradableEventId);
            case UNDEFINED -> throw new ResponseStatusException(HttpStatus.FORBIDDEN, INVALID_ROLE);
        };
    }

    private List<Student> getStudentListFromProjectGroup(Optional<ProjectGroup> projectGroupOptional) {
        return projectGroupRepository.getStudentsByProjectGroup(projectGroupOptional.orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, STUDENT_NOT_FOUND)));
    }

    private GradableEvent validateUsageAndGetGradableEventForTarget(Long gradableEventId, TargetRequestDto target) {
        GradableEvent gradableEvent = validateUsageAndGetGradableEvent(gradableEventId);
        gradableEventService.validateTargetGradableEventAccess(target, gradableEvent);
        return gradableEvent;
    }

    private GradableEvent validateUsageAndGetGradableEvent(Long gradableEventId) {
        GradableEvent gradableEvent = gradableEventService.getGradableEventById(gradableEventId);
        accessAuthorizer.authorizeCurrentUserCourseAccess(gradableEvent.getEventSection().getCourse().getId());

        if (gradableEvent.getEventSection().getEventSectionType() == EventSectionType.TEST) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Testy nie wspierają oddawania zadania.");
        }

        return gradableEvent;
    }
}
