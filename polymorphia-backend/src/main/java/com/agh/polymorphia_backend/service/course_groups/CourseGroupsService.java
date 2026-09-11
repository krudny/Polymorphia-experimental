package com.agh.polymorphia_backend.service.course_groups;

import com.agh.polymorphia_backend.dto.request.course_group.ChangeStudentCourseGroupRequestDto;
import com.agh.polymorphia_backend.dto.request.course_group.CreateCourseGroupRequestDto;
import com.agh.polymorphia_backend.dto.request.course_group.UpdateCourseGroupRequestDto;
import com.agh.polymorphia_backend.dto.response.course_groups.CourseGroupsResponseDto;
import com.agh.polymorphia_backend.dto.response.course_groups.CourseGroupsShortResponseDto;
import com.agh.polymorphia_backend.model.course.Course;
import com.agh.polymorphia_backend.model.course.CourseGroup;
import com.agh.polymorphia_backend.model.course.StudentCourseGroupAssignment;
import com.agh.polymorphia_backend.model.user.TeachingRoleUser;
import com.agh.polymorphia_backend.model.user.UserType;
import com.agh.polymorphia_backend.model.user.student.Animal;
import com.agh.polymorphia_backend.repository.course.CourseGroupRepository;
import com.agh.polymorphia_backend.repository.course.StudentCourseGroupRepository;
import com.agh.polymorphia_backend.repository.project.ProjectGroupRepository;
import com.agh.polymorphia_backend.repository.user.student.AnimalRepository;
import com.agh.polymorphia_backend.service.course.CourseService;
import com.agh.polymorphia_backend.service.mapper.CourseGroupsMapper;
import com.agh.polymorphia_backend.service.user.UserService;
import com.agh.polymorphia_backend.service.validation.AccessAuthorizer;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static com.agh.polymorphia_backend.service.user.UserService.INVALID_ROLE;

@Service
@AllArgsConstructor
public class CourseGroupsService {
    private static final String COURSE_GROUP_NOT_FOUND = "Nie znaleziono grupy zajęciowej.";
    private final CourseGroupRepository courseGroupRepository;
    private final UserService userService;
    private final AccessAuthorizer accessAuthorizer;
    private final CourseGroupsMapper courseGroupsMapper;
    private final CourseService courseService;
    private final AnimalRepository animalRepository;
    private final StudentCourseGroupRepository studentCourseGroupRepository;
    private final ProjectGroupRepository projectGroupRepository;

    public List<String> findAllCourseGroupNames(Long courseId) {
        accessAuthorizer.authorizeCurrentUserCourseAccess(courseId);
        return courseGroupRepository.findNamesByCourseId(courseId);
    }

    public List<CourseGroupsResponseDto> getAllCourseGroups(Long courseId) {
        accessAuthorizer.authorizeCurrentUserCourseAccess(courseId);
        return findCourseGroups(courseId, null, null);
    }

    public List<CourseGroupsShortResponseDto> getAllShortCourseGroups(Long courseId) {
        accessAuthorizer.authorizeCurrentUserCourseAccess(courseId);
        return findShortCourseGroups(courseId, null, null);
    }

    public List<CourseGroupsResponseDto> getIndividualCourseGroups(Long courseId) {
        return getCourseGroups(courseId);
    }

    public List<CourseGroupsShortResponseDto> getIndividualShortCourseGroups(Long courseId) {
        return getShortCourseGroups(courseId);
    }

    public CourseGroup findCourseGroupForTeachingRoleUser(Long courseGroupId) {
        return switch (userService.getCurrentUserRole()) {
            case INSTRUCTOR, COORDINATOR -> courseGroupRepository.findCourseGroupForTeachingRoleUser(courseGroupId,
                userService.getCurrentUser().getUserId()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, COURSE_GROUP_NOT_FOUND));
            default -> throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Brak uprawnień.");
        };
    }

    @Transactional
    public void createCourseGroup(CreateCourseGroupRequestDto requestDto) {
        accessAuthorizer.authorizeCurrentUserCourseAccess(requestDto.getCourseId());

        Course course = courseService.getCourseById(requestDto.getCourseId());
        TeachingRoleUser teachingRoleUser = userService.getTeachingRoleUser(requestDto.getTeachingRoleId(), requestDto.getCourseId());

        CourseGroup courseGroup = CourseGroup.builder()
                .name(requestDto.getName())
                .room(requestDto.getRoom())
                .course(course)
                .teachingRoleUser(teachingRoleUser)
                .studentCourseGroupAssignments(List.of())
                .build();

        try {
            courseGroupRepository.save(courseGroup);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Nie udało się utworzyć grupy zajęciowej.");
        }

    }

    @Transactional
    public void updateCourseGroup(Long courseGroupId, UpdateCourseGroupRequestDto requestDto) {
        CourseGroup courseGroup = getCourseGroupById(courseGroupId);
        Long courseId = courseGroup.getCourse().getId();
        accessAuthorizer.authorizeCurrentUserCourseAccess(courseId);

        TeachingRoleUser teachingRoleUser = userService.getTeachingRoleUser(requestDto.getTeachingRoleId(), courseId);

        courseGroup.setName(requestDto.getName());
        courseGroup.setRoom(requestDto.getRoom());
        courseGroup.setTeachingRoleUser(teachingRoleUser);

        try {
            courseGroupRepository.save(courseGroup);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Nie udało się zaktualizować grupy zajęciowej.");
        }
    }

    @Transactional
    public void changeStudentCourseGroup(ChangeStudentCourseGroupRequestDto requestDto) {
        Animal animal = animalRepository.findById(requestDto.getAnimalId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nie znaleziono zwierzaka."));

        StudentCourseGroupAssignment currentAssignment = animal.getStudentCourseGroupAssignment();
        if (currentAssignment == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Zwierzak nie jest przypisany do żadnej grupy.");
        }

        Long oldCourseGroupId = currentAssignment.getCourseGroup().getId();
        Long newCourseGroupId = requestDto.getNewCourseGroupId();

        CourseGroup newCourseGroup = getCourseGroupById(newCourseGroupId);

        if (!currentAssignment.getCourseGroup().getCourse().getId().equals(newCourseGroup.getCourse().getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Grupy należą do różnych kursów.");
        }

        studentCourseGroupRepository.updateStudentCourseGroupId(animal.getId(), oldCourseGroupId, newCourseGroupId);
    }


    @Transactional
    public void deleteCourseGroup(Long courseGroupId) {
        projectGroupRepository.deleteProjectGroupsByCourseGroupId(courseGroupId);
        animalRepository.deleteAnimalsByCourseGroupId(courseGroupId);
        courseGroupRepository.deleteById(courseGroupId);
    }

    private List<CourseGroupsResponseDto> getCourseGroups(Long courseId) {
        accessAuthorizer.authorizeCurrentUserCourseAccess(courseId);
        Long userId = userService.getCurrentUser().getUser().getId();
        UserType userRole = userService.getCurrentUserRole();

        return switch (userRole) {
            case STUDENT -> findCourseGroups(courseId, userId, null);
            case INSTRUCTOR, COORDINATOR -> findCourseGroups(courseId, null, userId);
            case UNDEFINED -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, INVALID_ROLE);
        };
    }

    private List<CourseGroupsShortResponseDto> getShortCourseGroups(Long courseId) {
        accessAuthorizer.authorizeCurrentUserCourseAccess(courseId);
        Long userId = userService.getCurrentUser().getUser().getId();
        UserType userRole = userService.getCurrentUserRole();

        return switch (userRole) {
            case STUDENT -> findShortCourseGroups(courseId, userId, null);
            case INSTRUCTOR, COORDINATOR -> findShortCourseGroups(courseId, null, userId);
            case UNDEFINED -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, INVALID_ROLE);
        };
    }

    private List<CourseGroupsResponseDto> findCourseGroups(Long courseId, Long userId, Long teachingRoleUserId) {
        return courseGroupRepository.findCourseGroups(courseId, userId, teachingRoleUserId).stream()
            .map(courseGroupsMapper::toCourseGroupResponseDto).toList();
    }

    private List<CourseGroupsShortResponseDto> findShortCourseGroups(Long courseId, Long userId,
        Long teachingRoleUserId) {
        return courseGroupRepository.findShortCourseGroups(courseId, userId, teachingRoleUserId).stream()
            .map(courseGroupsMapper::toCourseGroupShortResponseDto).toList();
    }

    private CourseGroup getCourseGroupById(Long courseGroupId) {
        return courseGroupRepository.findById(courseGroupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, COURSE_GROUP_NOT_FOUND));
    }
}
