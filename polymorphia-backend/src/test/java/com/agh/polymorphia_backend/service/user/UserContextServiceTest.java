package com.agh.polymorphia_backend.service.user;

import com.agh.polymorphia_backend.dto.response.user_context.BaseUserDetailsResponseDto;
import com.agh.polymorphia_backend.model.course.Course;
import com.agh.polymorphia_backend.model.user.User;
import com.agh.polymorphia_backend.model.user.UserType;
import com.agh.polymorphia_backend.model.user.student.Student;
import com.agh.polymorphia_backend.repository.user.UserRepository;
import com.agh.polymorphia_backend.service.course.CourseService;
import com.agh.polymorphia_backend.service.mapper.UserContextMapper;
import com.agh.polymorphia_backend.service.validation.AccessAuthorizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class UserContextServiceTest {
    @Mock
    private CourseService courseService;

    @Mock
    private AccessAuthorizer accessAuthorizer;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @Mock
    private UserContextMapper userContextMapper;

    @InjectMocks
    private UserContextService userContextService;

    private User user;
    private Student student;
    private Course course;
    private Long courseId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = User.builder()
                .id(1L)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .build();
        student = Student.builder()
                .user(user)
                .build();
        courseId = 10L;
        course = Course.builder()
                .id(courseId)
                .build();
    }

    @Test
    void getUserContext_shouldReturnMappedDto() {
        // given
        BaseUserDetailsResponseDto userDetails = BaseUserDetailsResponseDto.builder()
                .imageUrl("url")
                .fullName("user")
                .courseId(1L)
                .build();

        when(userService.getCurrentUser()).thenReturn(student);
        when(userService.getUserRole(student)).thenReturn(UserType.STUDENT);
        when(userContextMapper.toBaseUserDetailsResponseDto(student)).thenReturn(userDetails);

        // when
        var result = userContextService.getUserContext();

        // then
        assertEquals(UserType.STUDENT, result.getUserRole());
        assertEquals(userDetails, result.getUserDetails());
    }

    @Test
    void getUserRole_shouldReturnUserRole() {
        // given
        when(userService.getCurrentUser()).thenReturn(student);
        when(userService.getUserRole(student)).thenReturn(UserType.STUDENT);

        // when & then
        assertEquals(userContextService.getUserRole(), UserType.STUDENT);
    }

    @Test
    void setPreferredCourseId_shouldUpdatePreferredCourse() {
        // given
        when(courseService.getCourseById(10L)).thenReturn(course);
        when(accessAuthorizer.authorizePreferredCourseSwitch(courseId)).thenReturn(true);
        when(userService.getCurrentUser()).thenReturn(student);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // when
        userContextService.setPreferredCourseId(10L);

        // then
        verify(userRepository).save(user);
        assertEquals(course, user.getPreferredCourse());
        verify(userService).updateSecurityCredentials(user);
    }

    @Test
    void setPreferredCourseId_shouldReturnEarly_whenAccessNotAuthorized() {
        // given
        when(courseService.getCourseById(10L)).thenReturn(course);
        when(accessAuthorizer.authorizePreferredCourseSwitch(courseId)).thenReturn(false);

        // when
        userContextService.setPreferredCourseId(10L);

        // then
        verify(userService, never()).getCurrentUser();
        verify(userRepository, never()).findById(any());
        verify(userRepository, never()).save(any());
        verify(userService, never()).updateSecurityCredentials(any());
    }

    @Test
    void setPreferredCourseId_shouldThrowWhenUserNotFound() {
        // when
        when(courseService.getCourseById(10L)).thenReturn(course);
        when(accessAuthorizer.authorizePreferredCourseSwitch(courseId)).thenReturn(true);
        when(userService.getCurrentUser()).thenReturn(student);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // then
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> userContextService.setPreferredCourseId(10L)
        );

    }
}
