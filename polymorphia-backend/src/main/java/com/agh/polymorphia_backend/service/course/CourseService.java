package com.agh.polymorphia_backend.service.course;

import com.agh.polymorphia_backend.dto.response.user.TeachingRoleUserResponseDto;
import com.agh.polymorphia_backend.dto.response.user_context.AvailableCoursesResponseDto;
import com.agh.polymorphia_backend.model.course.Course;
import com.agh.polymorphia_backend.model.user.AbstractRoleUser;
import com.agh.polymorphia_backend.model.user.User;
import com.agh.polymorphia_backend.model.user.UserCourseRole;
import com.agh.polymorphia_backend.repository.course.CourseRepository;
import com.agh.polymorphia_backend.repository.event_section.EventSectionRepository;
import com.agh.polymorphia_backend.repository.user.UserCourseRoleRepository;
import com.agh.polymorphia_backend.repository.user.UserRepository;
import com.agh.polymorphia_backend.service.mapper.CourseMapper;
import com.agh.polymorphia_backend.service.user.UserService;
import com.agh.polymorphia_backend.service.validation.AccessAuthorizer;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CourseService {
    public static final String COURSE_NOT_FOUND = "Kurs nie istnieje lub nie masz uprawnień do jego przeglądania.";
    private final CourseRepository courseRepository;
    private final EventSectionRepository eventSectionRepository;
    private final UserService userService;
    private final CourseMapper courseMapper;
    private final UserCourseRoleRepository userCourseRoleRepository;
    private final AccessAuthorizer accessAuthorizer;
    private final UserRepository userRepository;

    public Course getCourseById(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, COURSE_NOT_FOUND));
    }

    public Long getCourseIdByEventSectionId(Long eventSectionId) {
        return eventSectionRepository.findCourseIdById(eventSectionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, COURSE_NOT_FOUND));
    }

    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    public List<AvailableCoursesResponseDto> getAvailableCourses() {
        AbstractRoleUser abstractRoleUser = userService.getCurrentUser();
        Long userId = abstractRoleUser.getUser().getId();
        List<Course> courses = getAllCourses();

        Map<Long, UserCourseRole> courseRoleMap = userCourseRoleRepository
                .findAllByUserId(userId)
                .stream()
                .collect(Collectors.toMap(
                        userCourseRole -> userCourseRole.getCourse().getId(),
                        userCourseRole -> userCourseRole
                ));

        return courses.stream()
                .filter(course -> courseRoleMap.containsKey(course.getId()))
                .filter(course -> accessAuthorizer.isCourseAccessAuthorized(abstractRoleUser, course.getId()))
                .map(course -> courseMapper.toAvailableCoursesResponseDto(
                        course,
                        courseRoleMap.get(course.getId()).getRole()
                ))
                .toList();

    }

    public List<TeachingRoleUserResponseDto> getTeachingRoleUsers(Long courseId) {
        accessAuthorizer.authorizeCurrentUserCourseAccess(courseId);

        List<User> users = userRepository.findAllTeachingRoleUsers();

        return users.stream()
                .map(user -> TeachingRoleUserResponseDto.builder()
                        .userId(user.getId())
                        .fullName(userService.getFullName(user))
                        .build())
                .toList();
    }
}
