package com.agh.polymorphia_backend.service.student;

import com.agh.polymorphia_backend.dto.request.student.CreateAnimalRequestDto;
import com.agh.polymorphia_backend.model.course.StudentCourseGroupAssignment;
import com.agh.polymorphia_backend.model.course.StudentCourseGroupAssignmentId;
import com.agh.polymorphia_backend.model.user.AbstractRoleUser;
import com.agh.polymorphia_backend.model.user.User;
import com.agh.polymorphia_backend.model.user.student.Animal;
import com.agh.polymorphia_backend.model.user.student.Student;
import com.agh.polymorphia_backend.repository.course.StudentCourseGroupRepository;
import com.agh.polymorphia_backend.repository.user.role.StudentRepository;
import com.agh.polymorphia_backend.repository.user.student.AnimalRepository;
import com.agh.polymorphia_backend.service.user.UserService;
import com.agh.polymorphia_backend.service.validation.AccessAuthorizer;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class AnimalService {
    public static final String ANIMAL_NOT_FOUND = "Zwierzak nie został znaleziony.";
    private final AnimalRepository animalRepository;
    private final UserService userService;
    private final StudentCourseGroupRepository studentCourseGroupRepository;
    private final StudentRepository studentRepository;
    private final AccessAuthorizer accessAuthorizer;

    public Animal getAnimal(Long userId, Long courseId) {
        return animalRepository.findByCourseIdAndStudentId(courseId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, ANIMAL_NOT_FOUND));
    }

    public Long getAnimalId(Long userId, Long courseId) {
        return animalRepository.findIdByCourseIdAndStudentId(courseId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, ANIMAL_NOT_FOUND));
    }

    public List<Animal> getAnimals(List<Long> studentIds, Long courseId) {
        List<Animal> animals = animalRepository.findByCourseIdAndStudentIds(courseId, studentIds);
        if(animals.size() != studentIds.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nie wszystkie zwierzaki zostały znalezione.");
        }
        return animals;
    }

    public Long getAnimalIdForCurrentUser(Long courseId) {
        AbstractRoleUser student = userService.getCurrentUser();
        return getAnimalId(student.getUserId(), courseId);
    }

    public Long validateAndGetAnimalId(Long courseId) {
        accessAuthorizer.authorizeCurrentUserCourseAccess(courseId);
        AbstractRoleUser student = userService.getCurrentUser();
        return getAnimalId(student.getUserId(), courseId);
    }

    public boolean hasAnimalInCourse(Long courseId) {
        User user = userService.getCurrentUser().getUser();
        return animalRepository.existsByCourseIdAndStudentId(courseId, user.getId());
    }

    public Long getAnimalIdForAssignedChest(Long assignedChestId){
        return animalRepository.findByAssignedChestId(assignedChestId);
    }

    public Long getStudentIdForAnimalId(Long animalId){
        return studentCourseGroupRepository.getStudentIdByAnimalId(animalId);
    }

    @Transactional
    public void createAnimal(CreateAnimalRequestDto requestDTO) {
        User user = userService.getCurrentUser().getUser();

        Student student = studentRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Student nie został znaleziony."));

        getAnimalByNameAndCourseId(requestDTO.getAnimalName(), requestDTO.getCourseId()).ifPresent(animal -> {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Zwierzak z tą nazwą już istnieje.");
        });

        Animal animal = Animal.builder()
                .name(requestDTO.getAnimalName())
                .build();

        try {
            StudentCourseGroupAssignmentId assignmentId = StudentCourseGroupAssignmentId.builder()
                    .studentId(student.getUserId())
                    .courseGroupId(requestDTO.getCourseGroupId())
                    .build();

            StudentCourseGroupAssignment assignment = studentCourseGroupRepository.findById(assignmentId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Student nie był zaproszony."));

            Animal savedAnimal = animalRepository.save(animal);

            studentCourseGroupRepository.save(assignment);
            assignment.setAnimal(savedAnimal);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Nie udało się utworzyć zwierzaka.");
        }
    }

    public Long countAnimalCourseGroups(List<Animal> animals){
        return animalRepository.countAnimalCourseGroups(animals);
    }

    private Optional<Animal> getAnimalByNameAndCourseId(String animalName, Long courseId) {
        return animalRepository.findByNameAndCourseId(animalName, courseId);
    }

    @Transactional
    public void deleteAnimal(Long animalId) {
        animalRepository.deleteAnimalById(animalId);
    }
}
