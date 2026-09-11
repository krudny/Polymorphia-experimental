package com.agh.polymorphia_backend.service.validation;

import com.agh.polymorphia_backend.model.user.AbstractRoleUser;
import com.agh.polymorphia_backend.model.user.User;
import com.agh.polymorphia_backend.model.user.UserCourseRole;
import com.agh.polymorphia_backend.model.user.UserType;
import com.agh.polymorphia_backend.repository.course.CourseRepository;
import com.agh.polymorphia_backend.repository.project.ProjectGroupRepository;
import com.agh.polymorphia_backend.repository.user.UserCourseRoleRepository;
import com.agh.polymorphia_backend.repository.user.role.InstructorRepository;
import com.agh.polymorphia_backend.repository.user.role.StudentRepository;
import com.agh.polymorphia_backend.repository.user.student.AnimalRepository;
import com.agh.polymorphia_backend.service.user.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static com.agh.polymorphia_backend.service.course.CourseService.COURSE_NOT_FOUND;

@Service
@AllArgsConstructor
public class AccessAuthorizer {
    private final UserService userService;
    private final UserCourseRoleRepository userCourseRoleRepository;
    private final InstructorRepository instructorRepository;
    private final StudentRepository studentRepository;
    private final AnimalRepository animalRepository;
    private final CourseRepository courseRepository;
    private final ProjectGroupRepository projectGroupRepository;

    public void authorizeCurrentUserCourseAccess(Long courseId) {
        AbstractRoleUser user = userService.getCurrentUser();

        if (!isCourseAccessAuthorized(user, courseId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, COURSE_NOT_FOUND);
        }
    }

    public void authorizeStudentDataAccess(Long courseId, Long studentId) {
        authorizeCurrentUserCourseAccess(courseId);
        Long userId = userService.getCurrentUser().getUser().getId();

        boolean authorized = switch (userService.getCurrentUserRole()) {
            case STUDENT -> userId.equals(studentId);
            case INSTRUCTOR -> instructorRepository.hasAccessToStudentInCourse(userId, courseId, studentId);
            case COORDINATOR -> isCourseAccessAuthorizedCoordinator(userId, courseId);
            case UNDEFINED -> false;
        };

        if (!authorized) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Brak dostępu do danych użytkownika.");
        }
    }

    public void authorizeProjectGroupDetailsAccess(Long groupId) {
        AbstractRoleUser user = userService.getCurrentUser();
        Long userId = user.getUser().getId();

        boolean authorized = switch (userService.getUserRole(user)) {
            case STUDENT -> projectGroupRepository.isStudentInProjectGroup(groupId, userId);
            case INSTRUCTOR -> projectGroupRepository.isTeachingRoleUserInProjectGroup(groupId, userId);
            case COORDINATOR -> isCourseAccessAuthorizedCoordinator(userId, projectGroupRepository.getCourseIdByProjectGroupId(groupId));
            case UNDEFINED -> false;
        };

        if (!authorized) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Brak dostępu do grupy projektowej.");
        }
    }

    public boolean authorizePreferredCourseSwitch(Long courseId) {
        User user = userService.getCurrentUser().getUser();

        UserCourseRole userCourseRole = userCourseRoleRepository
                .findByUserIdAndCourseId(user.getId(), courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nie znaleziono roli użytkownika w kursie."));

        if (userCourseRole.getRole() == UserType.STUDENT) {
            return animalRepository.existsByCourseIdAndStudentId(courseId, user.getId());
        }

        return true;
    }

    public boolean hasAnyRole(List<UserType> roles) {
        AbstractRoleUser user = userService.getCurrentUser();
        return roles.contains(userService.getUserRole(user));
    }

    public boolean isCourseAccessAuthorized(AbstractRoleUser roleUser, Long courseId) {
        Long userId = roleUser.getUser().getId();
        return switch (userService.getUserRole(roleUser)) {
            case STUDENT -> isCourseAccessAuthorizedStudent(userId, courseId);
            case INSTRUCTOR -> isCourseAccessAuthorizedInstructor(userId, courseId);
            case COORDINATOR -> isCourseAccessAuthorizedCoordinator(userId, courseId);
            case UNDEFINED -> isCourseAccessAuthorizedUndefined(userId, courseId);
        };
    }

    public void authorizeProjectGroupGrading(Long groupId) {
        Long userId = userService.getCurrentUser().getUser().getId();

        boolean authorized = switch (userService.getCurrentUserRole()) {
            case INSTRUCTOR -> projectGroupRepository.isTeachingRoleUserInProjectGroup(groupId, userId);
            case COORDINATOR -> isCourseAccessAuthorizedCoordinator(userId, projectGroupRepository.getCourseIdByProjectGroupId(groupId));
            default -> false;
        };

        if (!authorized) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Brak uprawnień do oceniania grupy projektowej.");
        }
    }

    private boolean isCourseAccessAuthorizedCoordinator(Long userId, Long courseId) {
        return courseRepository.isUserCoordinatorOfCourse(userId, courseId);
    }

    private boolean isCourseAccessAuthorizedInstructor(Long userId, Long courseId) {
        return instructorRepository.isUserTeachingRoleUserInCourse(userId, courseId);
    }

    private boolean isCourseAccessAuthorizedStudent(Long userId, Long courseId) {
        return studentRepository.isUserStudentAssignedToCourseGroup(userId, courseId);
    }

    private boolean isCourseAccessAuthorizedUndefined(Long userId, Long courseId) {
        return isCourseAccessAuthorizedStudent(userId, courseId)
                || isCourseAccessAuthorizedInstructor(userId, courseId)
                || isCourseAccessAuthorizedCoordinator(userId, courseId);
    }
}
