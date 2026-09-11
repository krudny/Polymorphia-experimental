package com.agh.polymorphia_backend.controller;

import com.agh.polymorphia_backend.dto.request.project.ProjectGroupConfigurationRequestDto;
import com.agh.polymorphia_backend.dto.request.project.ProjectGroupPickStudentsRequestDto;
import com.agh.polymorphia_backend.dto.request.target.TargetRequestDto;
import com.agh.polymorphia_backend.dto.response.project.*;
import com.agh.polymorphia_backend.dto.response.project.filter.ProjectGroupConfigurationFiltersResponseDto;
import com.agh.polymorphia_backend.dto.response.user_context.UserDetailsResponseDto;
import com.agh.polymorphia_backend.service.project.ProjectService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@AllArgsConstructor
@RequestMapping("/projects")
public class ProjectController {
    private final ProjectService projectService;

    @PostMapping("/variants")
    @PreAuthorize("hasAnyAuthority('STUDENT', 'INSTRUCTOR', 'COORDINATOR')")
    public ResponseEntity<List<ProjectVariantResponseDto>> getProjectVariants(@Valid @RequestBody TargetRequestDto target, @RequestParam Long projectId) {
        return ResponseEntity.ok(projectService.getProjectVariants(target, projectId));
    }

    @PostMapping("/variants/suggestions")
    @PreAuthorize("hasAnyAuthority('INSTRUCTOR', 'COORDINATOR')")
    public ResponseEntity<Map<Long, Long>> getSuggestedProjectVariants(@RequestBody Optional<TargetRequestDto> target, @RequestParam Long projectId) {
        return ResponseEntity.ok(projectService.getSuggestedProjectVariants(target, projectId));
    }

    @GetMapping("/group")
    @PreAuthorize("hasAnyAuthority('STUDENT', 'INSTRUCTOR', 'COORDINATOR')")
    public ResponseEntity<List<UserDetailsResponseDto>> getProjectGroupMembers(@RequestParam Long studentId, @RequestParam Long projectId) {
        return ResponseEntity.ok(projectService.getAnimalProjectGroup(studentId, projectId));
    }

    @DeleteMapping("/group")
    @PreAuthorize("hasAnyAuthority('INSTRUCTOR', 'COORDINATOR')")
    public ResponseEntity<Void> deleteProjectGroup(@Valid @RequestBody TargetRequestDto requestDto, @RequestParam Long projectId) {
        projectService.deleteProjectGroup(requestDto, projectId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/group/configuration")
    @PreAuthorize("hasAnyAuthority('INSTRUCTOR', 'COORDINATOR')")
    public ResponseEntity<ProjectGroupConfigurationResponseDto> getProjectGroupConfiguration(@Valid @RequestBody TargetRequestDto target, @RequestParam Long projectId) {
        return ResponseEntity.ok(projectService.getProjectGroupConfiguration(target, projectId));
    }

    @PutMapping("/group/configuration")
    @PreAuthorize("hasAnyAuthority('INSTRUCTOR', 'COORDINATOR')")
    public ResponseEntity<Void> processProjectGroupConfiguration(@Valid @RequestBody ProjectGroupConfigurationRequestDto requestDTO, @RequestParam Long projectId) {
        projectService.processProjectGroupConfiguration(requestDTO, projectId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/group/configuration/filters")
    @PreAuthorize("hasAnyAuthority('INSTRUCTOR', 'COORDINATOR')")
    public ResponseEntity<ProjectGroupConfigurationFiltersResponseDto> getProjectGroupConfigurationFilters(@RequestBody Optional<TargetRequestDto> target, @RequestParam Long projectId) {
        return ResponseEntity.ok(projectService.getProjectGroupConfigurationFilters(target, projectId));
    }

    @PostMapping("/group/students")
    @PreAuthorize("hasAnyAuthority('INSTRUCTOR', 'COORDINATOR')")
    public ResponseEntity<List<ProjectGroupPickStudentsResponseDto>> getProjectGroupConfigurationGroupPickStudents(@Valid @RequestBody ProjectGroupPickStudentsRequestDto requestDto, @RequestParam Long projectId) {
        return ResponseEntity.ok(projectService.getProjectGroupConfigurationGroupPickStudents(requestDto,projectId));
    }

    @GetMapping("/variants/categories")
    @PreAuthorize("hasAnyAuthority('INSTRUCTOR', 'COORDINATOR')")
    public ResponseEntity<List<ProjectCategoryWithVariantsResponseDto>> getProjectVariantCategories(@RequestParam Long projectId) {
        return ResponseEntity.ok(projectService.getProjectCategoryWithVariants(projectId));
    }
}
