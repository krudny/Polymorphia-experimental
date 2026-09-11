package com.agh.polymorphia_backend.controller;

import com.agh.polymorphia_backend.dto.request.course_import.CourseDetailsRequestDto;
import com.agh.polymorphia_backend.dto.response.user.TeachingRoleUserResponseDto;
import com.agh.polymorphia_backend.dto.response.user_context.AvailableCoursesResponseDto;
import com.agh.polymorphia_backend.exception.SkipUnexpectedExceptionHandler;
import com.agh.polymorphia_backend.service.course.CourseDetailsService;
import com.agh.polymorphia_backend.service.course.CourseImportService;
import com.agh.polymorphia_backend.service.course.CourseService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@RestController
@AllArgsConstructor
@RequestMapping("/courses")
public class CourseController {
    private final CourseService courseService;
    private final CourseDetailsService courseDetailsService;
    private final CourseImportService courseImportService;
    private final Validator validator;

    @GetMapping()
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AvailableCoursesResponseDto>> getAvailableCourses() {
        return ResponseEntity.ok(courseService.getAvailableCourses());
    }

    @GetMapping("/teaching-roles")
    @PreAuthorize("hasAuthority('COORDINATOR')")
    public ResponseEntity<List<TeachingRoleUserResponseDto>> getTeachingRoleUsers(@RequestParam Long courseId) {
        return ResponseEntity.ok(courseService.getTeachingRoleUsers(courseId));
    }

    @PostMapping()
    @PreAuthorize("hasAnyAuthority('INSTRUCTOR', 'COORDINATOR')")
    @SkipUnexpectedExceptionHandler
    public ResponseEntity<Void> createCourse(@Valid @RequestBody CourseDetailsRequestDto request) {
        courseImportService.importCourse(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyAuthority('INSTRUCTOR', 'COORDINATOR')")
    @SkipUnexpectedExceptionHandler
    public ResponseEntity<Void> createCourseFromFile(@RequestParam("file") MultipartFile file) throws IOException {
        CourseDetailsRequestDto request = getCourseDetailsRequestDto(file);
        courseImportService.importCourse(request);
        return ResponseEntity.noContent().build();
    }


    @PutMapping()
    @PreAuthorize("hasAnyAuthority('COORDINATOR')")
    @SkipUnexpectedExceptionHandler
    public ResponseEntity<Void> updateCourse(@Valid @RequestBody CourseDetailsRequestDto request,
                                             @RequestParam Long courseId) {
        courseImportService.updateCourse(request, courseId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping(value = "/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyAuthority('COORDINATOR')")
    @SkipUnexpectedExceptionHandler
    public ResponseEntity<Void> updateCourseFromFile(@RequestParam("file") MultipartFile file,
                                                     @RequestParam Long courseId) throws IOException {
        CourseDetailsRequestDto request = getCourseDetailsRequestDto(file);
        return updateCourse(request, courseId);
    }

    @GetMapping("/config")
    @PreAuthorize("hasAnyAuthority('COORDINATOR')")
    public ResponseEntity<CourseDetailsRequestDto> getCourseConfig(@RequestParam Long courseId) {
        return ResponseEntity.ok(courseDetailsService.getCourseDetails(courseId));
    }

    @GetMapping("/config/file")
    @PreAuthorize("hasAnyAuthority('COORDINATOR')")
    public ResponseEntity<byte[]> getCourseConfigAsFile(@RequestParam Long courseId) throws JsonProcessingException {
        CourseDetailsRequestDto courseDetails = courseDetailsService.getCourseDetails(courseId);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        byte[] jsonBytes = objectMapper.writeValueAsBytes(courseDetails);

        String filename = "course-" + courseId + "-config.json";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_JSON)
                .contentLength(jsonBytes.length)
                .body(jsonBytes);
    }

    private CourseDetailsRequestDto getCourseDetailsRequestDto(MultipartFile file) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        CourseDetailsRequestDto dto = objectMapper.readValue(file.getInputStream(), CourseDetailsRequestDto.class);

        Set<ConstraintViolation<CourseDetailsRequestDto>> violations = validator.validate(dto);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        return dto;
    }
}
