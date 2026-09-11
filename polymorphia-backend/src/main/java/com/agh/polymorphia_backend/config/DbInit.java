package com.agh.polymorphia_backend.config;

import com.agh.polymorphia_backend.dto.request.course_import.CourseDetailsRequestDto;
import com.agh.polymorphia_backend.model.user.User;
import com.agh.polymorphia_backend.model.user.coordinator.Coordinator;
import com.agh.polymorphia_backend.repository.user.UserRepository;
import com.agh.polymorphia_backend.repository.user.role.CoordinatorRepository;
import com.agh.polymorphia_backend.service.course.CourseImportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class DbInit {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final InitialUserProperties initialUserProperties;
    private final CourseImportService courseImportService;
    private final ObjectMapper objectMapper;
    private final CoordinatorRepository coordinatorRepository;
    @Value("${initial.course.config:}")
    private Resource initialCourseConfig;

    @PostConstruct
    public void init() {
        if (initialUserProperties.email() == null || initialUserProperties.password() == null) {
            return;
        }

        if (userRepository.findByEmail(initialUserProperties.email()).isEmpty()) {
            User user = User.builder()
                    .firstName(initialUserProperties.firstName())
                    .lastName(initialUserProperties.lastName())
                    .email(initialUserProperties.email())
                    .password(passwordEncoder.encode(initialUserProperties.password()))
                    .build();

            Coordinator coordinator = Coordinator.builder()
                    .user(user)
                    .build();

            coordinatorRepository.save(coordinator);
            createInitialCourse(user);
        }
    }

    private void createInitialCourse(User user) {
        if (initialCourseConfig != null && initialCourseConfig.exists()) {
            try {
                CourseDetailsRequestDto courseConfig = objectMapper.readValue(
                        initialCourseConfig.getInputStream(),
                        CourseDetailsRequestDto.class
                );

                courseImportService.importCourseForUser(courseConfig, user);
            } catch (IOException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Błąd tworzenia kursu.");
            }
        }
    }
}
