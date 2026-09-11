package com.agh.polymorphia_backend.service.validation;

import com.agh.polymorphia_backend.model.gradable_event.subtypes.task.TaskSubmission;
import com.agh.polymorphia_backend.model.user.AbstractRoleUser;
import com.agh.polymorphia_backend.model.user.UserType;
import com.agh.polymorphia_backend.service.gradable_event.GradableEventService;
import com.agh.polymorphia_backend.service.student.AnimalService;
import com.agh.polymorphia_backend.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class TaskAuthorizer {

    private final UserService userService;
    private final AccessAuthorizer accessAuthorizer;
    private final GradableEventService gradableEventService;
    private final AnimalService animalService;

    public void checkTaskAccess(Long taskId) {
        Long courseId = gradableEventService.getCourseIdByGradableEventId(taskId);
        accessAuthorizer.authorizeCurrentUserCourseAccess(courseId);
    }

    public void checkTaskSubmissionAccess(TaskSubmission taskSubmission) {
        checkTaskAccess(taskSubmission.getTask().getId());

        AbstractRoleUser currentUser = userService.getCurrentUser();
        UserType role = userService.getUserRole(currentUser);

        if (role == UserType.STUDENT) {
            Long currentUserId = currentUser.getUserId();
            Long animalStudentId = animalService.getStudentIdForAnimalId(taskSubmission.getAnimal().getId());

            if (animalStudentId == null || !animalStudentId.equals(currentUserId)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Nie znaleziono zgłoszenia.");
            }
        }
    }
}
