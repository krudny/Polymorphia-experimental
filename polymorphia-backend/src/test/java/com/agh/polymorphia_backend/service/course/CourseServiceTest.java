package com.agh.polymorphia_backend.service.course;

import com.agh.polymorphia_backend.dto.response.user_context.AvailableCoursesResponseDto;
import com.agh.polymorphia_backend.model.course.Course;
import com.agh.polymorphia_backend.model.user.AbstractRoleUser;
import com.agh.polymorphia_backend.model.user.User;
import com.agh.polymorphia_backend.model.user.UserCourseRole;
import com.agh.polymorphia_backend.model.user.UserType;
import com.agh.polymorphia_backend.model.user.student.Student;
import com.agh.polymorphia_backend.repository.course.CourseRepository;
import com.agh.polymorphia_backend.repository.event_section.EventSectionRepository;
import com.agh.polymorphia_backend.repository.user.UserCourseRoleRepository;
import com.agh.polymorphia_backend.repository.user.UserRepository;
import com.agh.polymorphia_backend.service.mapper.CourseMapper;
import com.agh.polymorphia_backend.service.user.UserService;
import com.agh.polymorphia_backend.service.validation.AccessAuthorizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static com.agh.polymorphia_backend.service.course.CourseService.COURSE_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
public class CourseServiceTest {
    private static final Long COURSE_ID = 99L;
    private static final Long USER_ID = 1L;
    private CourseService service;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private UserService userService;

    @Mock
    private CourseMapper courseMapper;

    @Mock
    private UserCourseRoleRepository userCourseRoleRepository;

    @Mock
    private EventSectionRepository eventSectionRepository;

    @Mock
    private AccessAuthorizer accessAuthorizer;

    @Mock
    private UserRepository userRepository;

    private User user;
    private AbstractRoleUser abstractRoleUser;
    private Course course1;
    private Course course2;
    private UserCourseRole userCourseRole1;
    private UserCourseRole userCourseRole2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new CourseService(
                courseRepository,
                eventSectionRepository,
                userService,
                courseMapper,
                userCourseRoleRepository,
                accessAuthorizer,
                userRepository
        );

        user = User.builder()
                .id(USER_ID)
                .email("test@example.com")
                .build();

        abstractRoleUser = Student.builder()
                .user(user)
                .build();

        course1 = Course.builder()
                .id(1L)
                .name("Sample course 1")
                .build();

        course2 = Course.builder()
                .id(2L)
                .name("Sample course 2")
                .build();

        userCourseRole1 = UserCourseRole.builder()
                .user(user)
                .course(course1)
                .role(UserType.STUDENT)
                .build();

        userCourseRole2 = UserCourseRole.builder()
                .user(user)
                .course(course2)
                .role(UserType.COORDINATOR)
                .build();
    }

    @Test
    public void getCourse_shouldThrow_whenCourseNotFound() {
        when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.getCourseById(COURSE_ID)
        );

        assertEquals(404, ex.getStatusCode().value());
        assertEquals(COURSE_NOT_FOUND, ex.getReason());
    }

    @Test
    public void getCourse_shouldNotThrow_whenCourseFound() {
        Course course = new Course();
        when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.of(course));

        Course result = assertDoesNotThrow(() -> service.getCourseById(COURSE_ID));
        assertEquals(course, result);
    }

    @Test
    public void getAvailableCourses_shouldReturnFilteredCourses_whenUserHasAccess() {
        // given
        AvailableCoursesResponseDto dto1 = AvailableCoursesResponseDto.builder()
                .id(1L)
                .name("Sample course 1")
                .coordinatorName("Sample coordinator")
                .imageUrl("Sample url")
                .userRole(UserType.STUDENT)
                .build();

        AvailableCoursesResponseDto dto2 = AvailableCoursesResponseDto.builder()
                .id(2L)
                .name("Sample course 1")
                .coordinatorName("Sample coordinator")
                .imageUrl("Sample url")
                .userRole(UserType.COORDINATOR)
                .build();

        when(userService.getCurrentUser()).thenReturn(abstractRoleUser);
        when(courseRepository.findAll()).thenReturn(List.of(course1, course2));
        when(userCourseRoleRepository.findAllByUserId(USER_ID))
                .thenReturn(List.of(userCourseRole1, userCourseRole2));
        when(accessAuthorizer.isCourseAccessAuthorized(abstractRoleUser, course1.getId())).thenReturn(true);
        when(accessAuthorizer.isCourseAccessAuthorized(abstractRoleUser, course2.getId())).thenReturn(true);
        when(courseMapper.toAvailableCoursesResponseDto(course1, UserType.STUDENT)).thenReturn(dto1);
        when(courseMapper.toAvailableCoursesResponseDto(course2, UserType.COORDINATOR)).thenReturn(dto2);

        // when
        List<AvailableCoursesResponseDto> result = service.getAvailableCourses();

        // then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(dto1, dto2);
        assertThat(result.getFirst().getId()).isEqualTo(1L);
        assertThat(result.getFirst().getName()).isEqualTo("Sample course 1");
        assertThat(result.getFirst().getUserRole()).isEqualTo(UserType.STUDENT);
        assertThat(result.get(1).getUserRole()).isEqualTo(UserType.COORDINATOR);
    }

    @Test
    public void getAvailableCourses_shouldReturnEmptyList_whenUserHasNoRoles() {
        // given
        when(userService.getCurrentUser()).thenReturn(abstractRoleUser);
        when(courseRepository.findAll()).thenReturn(List.of(course1, course2));
        when(userCourseRoleRepository.findAllByUserId(USER_ID)).thenReturn(List.of());

        // when
        List<AvailableCoursesResponseDto> result = service.getAvailableCourses();

        // then
        assertThat(result).isEmpty();
    }

    @Test
    public void getAvailableCourses_shouldFilterOutUnauthorizedCourses() {
        // given
        AvailableCoursesResponseDto dto1 = AvailableCoursesResponseDto.builder()
                .id(1L)
                .name("Sample course 1")
                .coordinatorName("Sample coordinator")
                .imageUrl("Sample url")
                .userRole(UserType.STUDENT)
                .userRole(UserType.UNDEFINED)
                .build();

        when(userService.getCurrentUser()).thenReturn(abstractRoleUser);
        when(courseRepository.findAll()).thenReturn(List.of(course1, course2));
        when(userCourseRoleRepository.findAllByUserId(USER_ID))
                .thenReturn(List.of(userCourseRole1, userCourseRole2));
        when(accessAuthorizer.isCourseAccessAuthorized(abstractRoleUser, course1.getId())).thenReturn(true);
        when(accessAuthorizer.isCourseAccessAuthorized(abstractRoleUser, course2.getId())).thenReturn(false);
        when(courseMapper.toAvailableCoursesResponseDto(course1, UserType.STUDENT)).thenReturn(dto1);

        // when
        List<AvailableCoursesResponseDto> result = service.getAvailableCourses();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst()).isEqualTo(dto1);
        assertThat(result.getFirst().getId()).isEqualTo(1L);
        assertThat(result.getFirst().getName()).isEqualTo("Sample course 1");
    }
}
