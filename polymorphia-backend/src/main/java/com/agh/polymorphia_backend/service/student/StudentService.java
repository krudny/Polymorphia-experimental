package com.agh.polymorphia_backend.service.student;

import com.agh.polymorphia_backend.dto.response.user.StudentActivityResponseDto;
import com.agh.polymorphia_backend.model.course.StudentCourseGroupAssignment;
import com.agh.polymorphia_backend.model.course.StudentCourseGroupAssignmentId;
import com.agh.polymorphia_backend.model.user.User;
import com.agh.polymorphia_backend.repository.course.StudentCourseGroupRepository;
import com.agh.polymorphia_backend.repository.grade.GradeRepository;
import com.agh.polymorphia_backend.repository.grade.projections.StudentActivityProjection;
import com.agh.polymorphia_backend.service.mapper.StudentDetailsMapper;
import com.agh.polymorphia_backend.service.user.UserService;
import com.agh.polymorphia_backend.service.validation.AccessAuthorizer;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@AllArgsConstructor
public class StudentService {
    private final UserService userService;
    private final StudentCourseGroupRepository studentCourseGroupRepository;
    private final AnimalService animalService;
    private final AccessAuthorizer accessAuthorizer;
    private final StudentDetailsMapper studentDetailsMapper;
    private final GradeRepository gradeRepository;

    public StudentCourseGroupAssignmentId getStudentCourseGroupAssignment(Long courseId) {
        User user = userService.getCurrentUser().getUser();
        Long studentId = user.getId();

        StudentCourseGroupAssignment assignment = studentCourseGroupRepository
                .findByIdStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Student nie został przypisany do żadnej grupy w ramach kursu."));

        return assignment.getId();
    }

    public List<StudentActivityResponseDto> getStudentActivity(Long studentId, Long courseId) {
        accessAuthorizer.authorizeStudentDataAccess(courseId, studentId);
        Long animalId = animalService.getAnimalId(studentId, courseId);
        List<StudentActivityProjection> projections = gradeRepository.findStudentActivity(animalId);
        return projections.stream()
                .map(studentDetailsMapper::studentActivityProjectionToDto)
                .toList();
    }
}
