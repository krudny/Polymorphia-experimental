package com.agh.polymorphia_backend.service.project;

import com.agh.polymorphia_backend.dto.response.user_context.UserDetailsResponseDto;
import com.agh.polymorphia_backend.model.project.ProjectGroup;
import com.agh.polymorphia_backend.model.user.UserType;
import com.agh.polymorphia_backend.repository.grade.GradeRepository;
import com.agh.polymorphia_backend.repository.project.ProjectGroupRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ProjectGroupService {
    private final ProjectGroupRepository projectGroupRepository;
    private final GradeRepository gradeRepository;

    public ProjectGroup findById(Long projectGroupId) {
        return projectGroupRepository.findById(projectGroupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nie znaleziono grupy projektowej."));
    }

    public void save(ProjectGroup projectGroup) {
        if (projectGroup.getId() != null && gradeRepository.hasGroupGrades(projectGroup)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Nie można edytować ocenionej grupy projektowej.");
        }
        projectGroupRepository.save(projectGroup);
    }

    public void delete(ProjectGroup projectGroup) {
        if (gradeRepository.hasGroupGrades(projectGroup)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Nie można usunąć ocenionej grupy projektowej.");
        }
        projectGroupRepository.delete(projectGroup);
    }

    public List<Long> getStudentIdsFromProjectGroup(Long projectGroupId) {
        return projectGroupRepository.getStudentIdsByProjectGroupId(projectGroupId);
    }

    public Optional<ProjectGroup> findByProjectIdAndStudentId(Long projectId, Long studentId) {
        return projectGroupRepository.getProjectGroupByProjectIdAndStudentId(projectId, studentId);
    }

    public List<UserDetailsResponseDto> getUserDetailsResponseByProjectGroup(Long projectGroupId) {
        return projectGroupRepository.getUserDetailsResponseByProjectGroupId(projectGroupId).stream()
                .map(userDetails -> UserDetailsResponseDto.builder()
                        .userRole(UserType.STUDENT)
                        .userDetails(userDetails)
                        .build())
                .toList();
    }
}
