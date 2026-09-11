package com.agh.polymorphia_backend.service.submission;

import com.agh.polymorphia_backend.BaseTest;
import com.agh.polymorphia_backend.dto.request.SubmissionDetailsRequestDto;
import com.agh.polymorphia_backend.dto.request.target.StudentGroupTargetRequestDto;
import com.agh.polymorphia_backend.dto.request.target.StudentTargetRequestDto;
import com.agh.polymorphia_backend.dto.request.target.TargetRequestDto;
import com.agh.polymorphia_backend.dto.response.submission.SubmissionDetailsDto;
import com.agh.polymorphia_backend.dto.response.submission.SubmissionRequirementResponseDto;
import com.agh.polymorphia_backend.model.course.Course;
import com.agh.polymorphia_backend.model.course.StudentCourseGroupAssignment;
import com.agh.polymorphia_backend.model.event_section.AssignmentSection;
import com.agh.polymorphia_backend.model.event_section.ProjectSection;
import com.agh.polymorphia_backend.model.event_section.TestSection;
import com.agh.polymorphia_backend.model.gradable_event.GradableEvent;
import com.agh.polymorphia_backend.model.project.ProjectGroup;
import com.agh.polymorphia_backend.model.submission.Submission;
import com.agh.polymorphia_backend.model.submission.SubmissionRequirement;
import com.agh.polymorphia_backend.model.user.UserType;
import com.agh.polymorphia_backend.model.user.coordinator.Coordinator;
import com.agh.polymorphia_backend.model.user.instructor.Instructor;
import com.agh.polymorphia_backend.model.user.student.Animal;
import com.agh.polymorphia_backend.model.user.student.Student;
import com.agh.polymorphia_backend.repository.grade.GradeRepository;
import com.agh.polymorphia_backend.repository.project.ProjectGroupRepository;
import com.agh.polymorphia_backend.repository.submission.SubmissionRepository;
import com.agh.polymorphia_backend.repository.submission.SubmissionRequirementRepository;
import com.agh.polymorphia_backend.repository.user.role.StudentRepository;
import com.agh.polymorphia_backend.service.gradable_event.GradableEventService;
import com.agh.polymorphia_backend.service.mapper.SubmissionMapper;
import com.agh.polymorphia_backend.service.student.AnimalService;
import com.agh.polymorphia_backend.service.user.UserService;
import com.agh.polymorphia_backend.service.validation.AccessAuthorizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class SubmissionServiceTest extends BaseTest {

    @Mock
    private GradableEventService gradableEventService;

    @Mock
    private AccessAuthorizer accessAuthorizer;

    @Mock
    private SubmissionRequirementRepository submissionRequirementRepository;

    @Spy
    private SubmissionMapper submissionMapper = new SubmissionMapper();

    @Mock
    private ProjectGroupRepository projectGroupRepository;

    @Mock
    private GradeRepository gradeRepository;

    @Mock
    private UserService userService;

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private AnimalService animalService;

    @Captor
    private ArgumentCaptor<List<Submission>> saveSubmissionCaptor;

    @Captor
    private ArgumentCaptor<List<Submission>> deleteSubmissionCaptor;

    @InjectMocks
    private SubmissionService submissionService;

    private GradableEvent assignment;
    private GradableEvent test;
    private GradableEvent project;
    private Course course;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        course = Course.builder().id(1L).build();

        AssignmentSection assignmentSection = AssignmentSection.builder()
            .id(10L)
            .course(course)
            .build();
        TestSection testSection = TestSection.builder()
            .id(11L)
            .course(course)
            .build();
        ProjectSection projectSection = ProjectSection.builder()
            .id(12L)
            .course(course)
            .build();

        assignment = GradableEvent.builder()
            .id(100L)
            .eventSection(assignmentSection)
            .build();
        test = GradableEvent.builder()
            .id(101L)
            .eventSection(testSection)
            .build();
        project = GradableEvent.builder()
            .id(102L)
            .eventSection(projectSection)
            .build();
    }

    @Test
    void shouldReturnSortedSubmissionRequirements() {
        SubmissionRequirement req1 = SubmissionRequirement.builder()
            .id(1L)
            .name("Req B")
            .orderIndex(2L)
            .build();
        SubmissionRequirement req2 = SubmissionRequirement.builder()
            .id(2L)
            .name("Req A")
            .orderIndex(1L)
            .build();

        when(
            gradableEventService.getGradableEventById(assignment.getId())
        ).thenReturn(assignment);
        doNothing()
            .when(accessAuthorizer)
            .authorizeCurrentUserCourseAccess(course.getId());
        when(
            submissionRequirementRepository.getSubmissionRequirementsByGradableEvent(
                assignment
            )
        ).thenReturn(List.of(req1, req2));

        List<SubmissionRequirementResponseDto> result =
            submissionService.getSubmissionRequirements(assignment.getId());

        assertThat(result).hasSize(2);
        assertThat(result.get(0).orderIndex()).isEqualTo(1L);
        assertThat(result.get(0).name()).isEqualTo("Req A");
        assertThat(result.get(1).orderIndex()).isEqualTo(2L);
        assertThat(result.get(1).name()).isEqualTo("Req B");
        verify(accessAuthorizer).authorizeCurrentUserCourseAccess(course.getId());
    }

    @Test
    void shouldThrowWhenEventSectionTypeIsTest() {
        when(
            gradableEventService.getGradableEventById(test.getId())
        ).thenReturn(test);
        ResponseStatusException ex = assertThrows(
            ResponseStatusException.class,
            () -> submissionService.getSubmissionRequirements(test.getId())
        );
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals(
            "Testy nie wspierają oddawania zadania.",
            ex.getReason()
        );
    }

    @Test
    void shouldThrowWhenAccessNotAuthorized() {
        when(
            gradableEventService.getGradableEventById(assignment.getId())
        ).thenReturn(assignment);
        doThrow(
            new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Nie znaleziono kursu."
            )
        )
            .when(accessAuthorizer)
            .authorizeCurrentUserCourseAccess(course.getId());
        ResponseStatusException ex = assertThrows(
            ResponseStatusException.class,
            () ->
                submissionService.getSubmissionRequirements(assignment.getId())
        );
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals("Nie znaleziono kursu.", ex.getReason());
    }

    @Test
    void shouldReturnEmptyListWhenNoRequirements() {
        when(
            gradableEventService.getGradableEventById(assignment.getId())
        ).thenReturn(assignment);
        doNothing()
            .when(accessAuthorizer)
            .authorizeCurrentUserCourseAccess(course.getId());
        when(
            submissionRequirementRepository.getSubmissionRequirementsByGradableEvent(
                assignment
            )
        ).thenReturn(List.of());
        List<SubmissionRequirementResponseDto> result =
            submissionService.getSubmissionRequirements(assignment.getId());
        assertThat(result).isEmpty();
    }

    @Nested
    class GetSubmissionDetailsTests {

        @Test
        void getSubmissionDetails_whenStudentAndAssignment_shouldReturnDetails() {
            Student student = Student.builder().build();
            student.setUserId(1L);
            TargetRequestDto target = new StudentTargetRequestDto(
                student.getUserId()
            );
            List<SubmissionRequirement> requirements = List.of(
                SubmissionRequirement.builder().id(1L).build()
            );
            List<Submission> submissions = List.of(
                Submission.builder()
                    .submissionRequirement(requirements.getFirst())
                    .url("http://example.com")
                    .build()
            );

            when(userService.getCurrentUserRole()).thenReturn(UserType.STUDENT);
            when(userService.getCurrentUser()).thenReturn(student);
            when(
                gradableEventService.getGradableEventById(assignment.getId())
            ).thenReturn(assignment);
            when(
                submissionRequirementRepository.getSubmissionRequirementsByGradableEvent(
                    assignment
                )
            ).thenReturn(requirements);
            when(
                submissionRepository.getSubmissionsByGradableEventAndStudent(
                    assignment.getId(),
                    student.getUserId()
                )
            ).thenReturn(submissions);

            Map<Long, SubmissionDetailsDto> result =
                submissionService.getSubmissionDetails(
                    assignment.getId(),
                    target
                ).details();

            assertThat(result).isNotNull();
            assertThat(result).hasSize(1);
            assertThat(result.get(1L).url()).isEqualTo("http://example.com");
            verify(accessAuthorizer).authorizeCurrentUserCourseAccess(course.getId());
            verify(gradableEventService).validateTargetGradableEventAccess(target, assignment);
        }

        @Test
        void getSubmissionDetails_whenStudentTargetsOtherStudent_shouldThrowException() {
            Student currentUser = Student.builder().build();
            currentUser.setUserId(1L);
            long otherStudentId = 2L;
            TargetRequestDto target = new StudentTargetRequestDto(
                otherStudentId
            );

            when(userService.getCurrentUserRole()).thenReturn(UserType.STUDENT);
            when(userService.getCurrentUser()).thenReturn(currentUser);
            when(
                gradableEventService.getGradableEventById(assignment.getId())
            ).thenReturn(assignment);

            ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () ->
                    submissionService.getSubmissionDetails(
                        assignment.getId(),
                        target
                    )
            );
            assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
            assertEquals("Nie znaleziono studenta.", ex.getReason());
        }
    }

    @Nested
    class PutSubmissionDetailsTests {

        @Test
        void putSubmissionDetails_whenStudentCreatesSubmission_shouldSaveSubmission() {
            Student student = Student.builder().build();
            student.setUserId(1L);
            TargetRequestDto target = new StudentTargetRequestDto(
                student.getUserId()
            );
            long requirementId = 1L;
            SubmissionDetailsDto detailsDto = SubmissionDetailsDto.builder()
                .url("http://new.com")
                .isLocked(false)
                .build();
            SubmissionDetailsRequestDto requestDto =
                new SubmissionDetailsRequestDto(
                    target,
                    Map.of(requirementId, detailsDto)
                );
            SubmissionRequirement requirement = SubmissionRequirement.builder()
                .id(requirementId)
                .isMandatory(true)
                .build();

            when(userService.getCurrentUserRole()).thenReturn(UserType.STUDENT);
            when(userService.getCurrentUser()).thenReturn(student);
            when(
                gradableEventService.getGradableEventById(assignment.getId())
            ).thenReturn(assignment);
            when(
                submissionRequirementRepository.getSubmissionRequirementsByGradableEvent(
                    assignment
                )
            ).thenReturn(List.of(requirement));
            when(
                submissionRepository.getSubmissionsByGradableEventAndStudent(
                    assignment.getId(),
                    student.getUserId()
                )
            ).thenReturn(Collections.emptyList());
            when(animalService.getAnimal(anyLong(), anyLong())).thenReturn(
                Animal.builder().id(1L).build()
            );

            submissionService.putSubmissionDetails(
                assignment.getId(),
                requestDto
            );

            verify(submissionRepository).saveAll(
                saveSubmissionCaptor.capture()
            );
            verify(submissionRepository).deleteAll(
                deleteSubmissionCaptor.capture()
            );
            List<Submission> savedSubmissions = saveSubmissionCaptor.getValue();
            assertThat(savedSubmissions).hasSize(1);
            Submission savedSubmission = savedSubmissions.getFirst();
            assertThat(savedSubmission.getUrl()).isEqualTo("http://new.com");
            assertThat(savedSubmission.isLocked()).isFalse();
            assertThat(
                savedSubmission.getSubmissionRequirement().getId()
            ).isEqualTo(requirementId);
            assertThat(savedSubmission.getAnimal().getId()).isEqualTo(1L);
            List<Submission> deletedSubmissions =
                deleteSubmissionCaptor.getValue();
            assertThat(deletedSubmissions).isEmpty();
        }

        @Test
        void putSubmissionDetails_whenStudentUpdatesLockedSubmission_shouldThrowException() {
            Student student = Student.builder().build();
            student.setUserId(1L);
            TargetRequestDto target = new StudentTargetRequestDto(
                student.getUserId()
            );
            long requirementId = 1L;
            SubmissionDetailsDto detailsDto = SubmissionDetailsDto.builder()
                .url("http://new.com")
                .isLocked(false)
                .build();
            SubmissionDetailsRequestDto requestDto =
                new SubmissionDetailsRequestDto(
                    target,
                    Map.of(requirementId, detailsDto)
                );
            SubmissionRequirement requirement = SubmissionRequirement.builder()
                .id(requirementId)
                .build();
            Submission existingSubmission = Submission.builder()
                .id(1L)
                .submissionRequirement(requirement)
                .isLocked(true)
                .url("http://old.com")
                .build();

            when(userService.getCurrentUserRole()).thenReturn(UserType.STUDENT);
            when(userService.getCurrentUser()).thenReturn(student);
            when(
                gradableEventService.getGradableEventById(assignment.getId())
            ).thenReturn(assignment);
            when(
                submissionRequirementRepository.getSubmissionRequirementsByGradableEvent(
                    assignment
                )
            ).thenReturn(List.of(requirement));
            when(
                submissionRepository.getSubmissionsByGradableEventAndStudent(
                    assignment.getId(),
                    student.getUserId()
                )
            ).thenReturn(List.of(existingSubmission));

            ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () ->
                    submissionService.putSubmissionDetails(
                        assignment.getId(),
                        requestDto
                    )
            );
            assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
            assertEquals("Nie można zmienić zablokowanego zgłoszenia.", ex.getReason());
        }

        @Test
        void putSubmissionDetails_whenStudentDeletesSubmission_shouldDeleteSubmission() {
            Student student = Student.builder().build();
            student.setUserId(1L);
            TargetRequestDto target = new StudentTargetRequestDto(
                student.getUserId()
            );
            long requirementId = 1L;
            SubmissionDetailsDto detailsDto = SubmissionDetailsDto.builder()
                .url("")
                .isLocked(false)
                .build();
            SubmissionDetailsRequestDto requestDto =
                new SubmissionDetailsRequestDto(
                    target,
                    Map.of(requirementId, detailsDto)
                );
            SubmissionRequirement requirement = SubmissionRequirement.builder()
                .id(requirementId)
                .isMandatory(false)
                .build();
            Submission existingSubmission = Submission.builder()
                .id(123L)
                .submissionRequirement(requirement)
                .isLocked(false)
                .url("http://old.com")
                .build();

            when(userService.getCurrentUserRole()).thenReturn(UserType.STUDENT);
            when(userService.getCurrentUser()).thenReturn(student);
            when(
                gradableEventService.getGradableEventById(assignment.getId())
            ).thenReturn(assignment);
            when(
                submissionRequirementRepository.getSubmissionRequirementsByGradableEvent(
                    assignment
                )
            ).thenReturn(List.of(requirement));
            when(
                submissionRepository.getSubmissionsByGradableEventAndStudent(
                    assignment.getId(),
                    student.getUserId()
                )
            ).thenReturn(List.of(existingSubmission));

            submissionService.putSubmissionDetails(
                assignment.getId(),
                requestDto
            );

            verify(submissionRepository).saveAll(
                saveSubmissionCaptor.capture()
            );
            verify(submissionRepository).deleteAll(
                deleteSubmissionCaptor.capture()
            );
            List<Submission> savedSubmissions = saveSubmissionCaptor.getValue();
            assertThat(savedSubmissions).isEmpty();
            List<Submission> deletedSubmissions =
                deleteSubmissionCaptor.getValue();
            assertThat(deletedSubmissions).hasSize(1);
            assertThat(deletedSubmissions.getFirst().getId()).isEqualTo(123L);
        }

        @Test
        void putSubmissionDetails_whenInstructorLocksSubmission_shouldUpdateAndLock() {
            Instructor instructor = Instructor.builder().build();
            instructor.setUserId(99L);
            Student student = Student.builder().build();
            student.setUserId(1L);
            TargetRequestDto target = new StudentTargetRequestDto(
                student.getUserId()
            );
            long requirementId = 1L;
            Animal animal = Animal.builder().id(1L).build();
            Submission existingSubmission = Submission.builder()
                .id(1L)
                .submissionRequirement(
                    SubmissionRequirement.builder().id(requirementId).build()
                )
                .isLocked(false)
                .url("http://old-url.com")
                .animal(animal)
                .build();
            SubmissionDetailsDto detailsDto = SubmissionDetailsDto.builder()
                .url(existingSubmission.getUrl())
                .isLocked(true)
                .build();
            SubmissionDetailsRequestDto requestDto =
                new SubmissionDetailsRequestDto(
                    target,
                    Map.of(requirementId, detailsDto)
                );

            when(userService.getCurrentUserRole()).thenReturn(
                UserType.INSTRUCTOR
            );
            when(userService.getCurrentUser()).thenReturn(instructor);
            when(
                gradableEventService.getGradableEventById(assignment.getId())
            ).thenReturn(assignment);
            when(
                studentRepository.findByUserIdAndGradableEventIdAndCourseGroupTeachingRoleUserId(
                    student.getUserId(),
                    assignment.getId(),
                    instructor.getUserId()
                )
            ).thenReturn(Optional.of(student));
            when(
                submissionRequirementRepository.getSubmissionRequirementsByGradableEvent(
                    assignment
                )
            ).thenReturn(
                List.of(existingSubmission.getSubmissionRequirement())
            );
            when(
                submissionRepository.getSubmissionsByGradableEventAndStudent(
                    assignment.getId(),
                    student.getUserId()
                )
            ).thenReturn(List.of(existingSubmission));

            submissionService.putSubmissionDetails(
                assignment.getId(),
                requestDto
            );

            verify(submissionRepository).saveAll(
                saveSubmissionCaptor.capture()
            );
            verify(submissionRepository).deleteAll(
                deleteSubmissionCaptor.capture()
            );
            List<Submission> savedSubmissions = saveSubmissionCaptor.getValue();
            assertThat(savedSubmissions).hasSize(1);
            Submission savedSubmission = savedSubmissions.getFirst();
            assertThat(savedSubmission.getUrl()).isEqualTo(
                existingSubmission.getUrl()
            );
            assertThat(savedSubmission.isLocked()).isTrue();
            assertThat(savedSubmission.getAnimal().getId()).isEqualTo(1L);
            List<Submission> deletedSubmissions =
                deleteSubmissionCaptor.getValue();
            assertThat(deletedSubmissions).isEmpty();
        }

        @Test
        void putSubmissionDetails_whenRequestIsMissingRequirement_shouldThrowException() {
            Student student = Student.builder().build();
            student.setUserId(1L);
            TargetRequestDto target = new StudentTargetRequestDto(
                student.getUserId()
            );
            SubmissionRequirement requirement1 = SubmissionRequirement.builder()
                .id(1L)
                .build();
            SubmissionRequirement requirement2 = SubmissionRequirement.builder()
                .id(2L)
                .build();
            SubmissionDetailsDto detailsDto = SubmissionDetailsDto.builder()
                .url("http://any.com")
                .isLocked(false)
                .build();
            SubmissionDetailsRequestDto requestDto =
                new SubmissionDetailsRequestDto(
                    target,
                    Map.of(requirement1.getId(), detailsDto)
                );

            when(userService.getCurrentUserRole()).thenReturn(UserType.STUDENT);
            when(userService.getCurrentUser()).thenReturn(student);
            when(
                gradableEventService.getGradableEventById(assignment.getId())
            ).thenReturn(assignment);
            when(
                submissionRequirementRepository.getSubmissionRequirementsByGradableEvent(
                    assignment
                )
            ).thenReturn(List.of(requirement1, requirement2));

            ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () ->
                    submissionService.putSubmissionDetails(
                        assignment.getId(),
                        requestDto
                    )
            );
            assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
            assertEquals(
                "Szczegóły oddania zadania muszą zawierać wszystkie wymagania.",
                ex.getReason()
            );
        }

        @Test
        void putSubmissionDetails_whenStudentChangesLock_shouldThrowException() {
            Student student = Student.builder().build();
            student.setUserId(1L);
            TargetRequestDto target = new StudentTargetRequestDto(
                student.getUserId()
            );
            long requirementId = 1L;
            Submission existingSubmission = Submission.builder()
                .id(1L)
                .submissionRequirement(
                    SubmissionRequirement.builder().id(requirementId).build()
                )
                .isLocked(false)
                .url("http://old.com")
                .build();
            SubmissionDetailsDto detailsDto = SubmissionDetailsDto.builder()
                .url(existingSubmission.getUrl())
                .isLocked(true)
                .build();
            SubmissionDetailsRequestDto requestDto =
                new SubmissionDetailsRequestDto(
                    target,
                    Map.of(requirementId, detailsDto)
                );

            when(userService.getCurrentUserRole()).thenReturn(UserType.STUDENT);
            when(userService.getCurrentUser()).thenReturn(student);
            when(
                gradableEventService.getGradableEventById(assignment.getId())
            ).thenReturn(assignment);
            when(
                submissionRequirementRepository.getSubmissionRequirementsByGradableEvent(
                    assignment
                )
            ).thenReturn(
                List.of(existingSubmission.getSubmissionRequirement())
            );
            when(
                submissionRepository.getSubmissionsByGradableEventAndStudent(
                    assignment.getId(),
                    student.getUserId()
                )
            ).thenReturn(List.of(existingSubmission));

            ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () ->
                    submissionService.putSubmissionDetails(
                        assignment.getId(),
                        requestDto
                    )
            );
            assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
            assertEquals(
                "Studenci nie mogą zmieniać statusu blokady.",
                ex.getReason()
            );
        }

        @Test
        void putSubmissionDetails_whenStudentDeletesMandatorySubmission_shouldThrowException() {
            Student student = Student.builder().build();
            student.setUserId(1L);
            TargetRequestDto target = new StudentTargetRequestDto(
                student.getUserId()
            );
            long requirementId = 1L;
            SubmissionDetailsDto detailsDto = SubmissionDetailsDto.builder()
                .url("")
                .isLocked(false)
                .build();
            SubmissionDetailsRequestDto requestDto =
                new SubmissionDetailsRequestDto(
                    target,
                    Map.of(requirementId, detailsDto)
                );
            SubmissionRequirement requirement = SubmissionRequirement.builder()
                .id(requirementId)
                .isMandatory(true)
                .build();
            Submission existingSubmission = Submission.builder()
                .id(123L)
                .submissionRequirement(requirement)
                .isLocked(false)
                .url("http://old.com")
                .build();

            when(userService.getCurrentUserRole()).thenReturn(UserType.STUDENT);
            when(userService.getCurrentUser()).thenReturn(student);
            when(
                gradableEventService.getGradableEventById(assignment.getId())
            ).thenReturn(assignment);
            when(
                submissionRequirementRepository.getSubmissionRequirementsByGradableEvent(
                    assignment
                )
            ).thenReturn(List.of(requirement));
            when(
                submissionRepository.getSubmissionsByGradableEventAndStudent(
                    assignment.getId(),
                    student.getUserId()
                )
            ).thenReturn(List.of(existingSubmission));

            ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () ->
                    submissionService.putSubmissionDetails(
                        assignment.getId(),
                        requestDto
                    )
            );
            assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
            assertEquals("Brak obowiązkowego wymagania.", ex.getReason());
        }

        @Test
        void putSubmissionDetails_whenBatchHasEmptyMandatory_shouldThrowException() {
            Student student = Student.builder().build();
            student.setUserId(1L);
            TargetRequestDto target = new StudentTargetRequestDto(
                student.getUserId()
            );
            SubmissionRequirement mandatoryReq = SubmissionRequirement.builder()
                .id(1L)
                .isMandatory(true)
                .build();
            SubmissionRequirement optionalReq = SubmissionRequirement.builder()
                .id(2L)
                .isMandatory(false)
                .build();

            SubmissionDetailsDto emptyDetails = SubmissionDetailsDto.builder()
                .url("")
                .isLocked(false)
                .build();
            SubmissionDetailsDto updatedDetails = SubmissionDetailsDto.builder()
                .url("http://new.com")
                .isLocked(false)
                .build();
            SubmissionDetailsRequestDto requestDto =
                new SubmissionDetailsRequestDto(
                    target,
                    Map.of(
                        mandatoryReq.getId(),
                        emptyDetails,
                        optionalReq.getId(),
                        updatedDetails
                    )
                );

            when(userService.getCurrentUserRole()).thenReturn(UserType.STUDENT);
            when(userService.getCurrentUser()).thenReturn(student);
            when(
                gradableEventService.getGradableEventById(assignment.getId())
            ).thenReturn(assignment);
            when(
                submissionRequirementRepository.getSubmissionRequirementsByGradableEvent(
                    assignment
                )
            ).thenReturn(List.of(mandatoryReq, optionalReq));

            ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () ->
                    submissionService.putSubmissionDetails(
                        assignment.getId(),
                        requestDto
                    )
            );
            assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
            assertEquals("Brak obowiązkowego wymagania.", ex.getReason());
        }

        @Test
        void putSubmissionDetails_whenDataIsUnchanged_shouldDoNothing() {
            Student student = Student.builder().build();
            student.setUserId(1L);
            TargetRequestDto target = new StudentTargetRequestDto(
                student.getUserId()
            );
            long requirementId = 1L;
            Submission existingSubmission = Submission.builder()
                .id(1L)
                .submissionRequirement(
                    SubmissionRequirement.builder().id(requirementId).build()
                )
                .isLocked(false)
                .url("http://old.com")
                .build();
            SubmissionDetailsDto detailsDto = SubmissionDetailsDto.builder()
                .url(existingSubmission.getUrl())
                .isLocked(existingSubmission.isLocked())
                .build();
            SubmissionDetailsRequestDto requestDto =
                new SubmissionDetailsRequestDto(
                    target,
                    Map.of(requirementId, detailsDto)
                );

            when(userService.getCurrentUserRole()).thenReturn(UserType.STUDENT);
            when(userService.getCurrentUser()).thenReturn(student);
            when(
                gradableEventService.getGradableEventById(assignment.getId())
            ).thenReturn(assignment);
            when(
                submissionRequirementRepository.getSubmissionRequirementsByGradableEvent(
                    assignment
                )
            ).thenReturn(
                List.of(existingSubmission.getSubmissionRequirement())
            );
            when(
                submissionRepository.getSubmissionsByGradableEventAndStudent(
                    assignment.getId(),
                    student.getUserId()
                )
            ).thenReturn(List.of(existingSubmission));

            submissionService.putSubmissionDetails(
                assignment.getId(),
                requestDto
            );

            verify(submissionRepository).saveAll(
                saveSubmissionCaptor.capture()
            );
            verify(submissionRepository).deleteAll(
                deleteSubmissionCaptor.capture()
            );
            assertThat(saveSubmissionCaptor.getValue()).isEmpty();
            assertThat(deleteSubmissionCaptor.getValue()).isEmpty();
        }

        @Test
        void putSubmissionDetails_whenNewSubmissionIsEmpty_shouldNotBeCreated() {
            Student student = Student.builder().build();
            student.setUserId(1L);
            TargetRequestDto target = new StudentTargetRequestDto(
                student.getUserId()
            );
            long requirementId = 1L;
            SubmissionDetailsDto detailsDto = SubmissionDetailsDto.builder()
                .url("")
                .isLocked(false)
                .build();
            SubmissionDetailsRequestDto requestDto =
                new SubmissionDetailsRequestDto(
                    target,
                    Map.of(requirementId, detailsDto)
                );
            SubmissionRequirement requirement = SubmissionRequirement.builder()
                .id(requirementId)
                .isMandatory(false)
                .build();

            when(userService.getCurrentUserRole()).thenReturn(UserType.STUDENT);
            when(userService.getCurrentUser()).thenReturn(student);
            when(
                gradableEventService.getGradableEventById(assignment.getId())
            ).thenReturn(assignment);
            when(
                submissionRequirementRepository.getSubmissionRequirementsByGradableEvent(
                    assignment
                )
            ).thenReturn(List.of(requirement));
            when(
                submissionRepository.getSubmissionsByGradableEventAndStudent(
                    assignment.getId(),
                    student.getUserId()
                )
            ).thenReturn(Collections.emptyList());

            submissionService.putSubmissionDetails(
                assignment.getId(),
                requestDto
            );

            verify(submissionRepository).saveAll(
                saveSubmissionCaptor.capture()
            );
            verify(submissionRepository).deleteAll(
                deleteSubmissionCaptor.capture()
            );
            assertThat(saveSubmissionCaptor.getValue()).isEmpty();
            assertThat(deleteSubmissionCaptor.getValue()).isEmpty();
        }

        @Test
        void putSubmissionDetails_whenStudentCreatesLockedSubmission_shouldThrowException() {
            Student student = Student.builder().build();
            student.setUserId(1L);
            TargetRequestDto target = new StudentTargetRequestDto(
                student.getUserId()
            );
            long requirementId = 1L;
            SubmissionDetailsDto detailsDto = SubmissionDetailsDto.builder()
                .url("http://new.com")
                .isLocked(true)
                .build();
            SubmissionDetailsRequestDto requestDto =
                new SubmissionDetailsRequestDto(
                    target,
                    Map.of(requirementId, detailsDto)
                );
            SubmissionRequirement requirement = SubmissionRequirement.builder()
                .id(requirementId)
                .build();

            when(userService.getCurrentUserRole()).thenReturn(UserType.STUDENT);
            when(userService.getCurrentUser()).thenReturn(student);
            when(
                gradableEventService.getGradableEventById(assignment.getId())
            ).thenReturn(assignment);
            when(
                submissionRequirementRepository.getSubmissionRequirementsByGradableEvent(
                    assignment
                )
            ).thenReturn(List.of(requirement));
            when(
                submissionRepository.getSubmissionsByGradableEventAndStudent(
                    assignment.getId(),
                    student.getUserId()
                )
            ).thenReturn(Collections.emptyList());

            ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () ->
                    submissionService.putSubmissionDetails(
                        assignment.getId(),
                        requestDto
                    )
            );
            assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
            assertEquals(
                "Studenci nie mogą zmieniać statusu blokady.",
                ex.getReason()
            );
        }

        @Test
        void putSubmissionDetails_whenCoordinatorLocksSubmission_shouldUpdateAndLock() {
            var coordinator = new Coordinator();
            coordinator.setUserId(99L);
            Student student = Student.builder().build();
            student.setUserId(1L);
            TargetRequestDto target = new StudentTargetRequestDto(
                student.getUserId()
            );
            long requirementId = 1L;
            Animal animal = Animal.builder().id(1L).build();
            Submission existingSubmission = Submission.builder()
                .id(1L)
                .submissionRequirement(
                    SubmissionRequirement.builder().id(requirementId).build()
                )
                .isLocked(false)
                .url("http://old-url.com")
                .animal(animal)
                .build();
            SubmissionDetailsDto detailsDto = SubmissionDetailsDto.builder()
                .url(existingSubmission.getUrl())
                .isLocked(true)
                .build();
            SubmissionDetailsRequestDto requestDto =
                new SubmissionDetailsRequestDto(
                    target,
                    Map.of(requirementId, detailsDto)
                );

            when(userService.getCurrentUserRole()).thenReturn(
                UserType.COORDINATOR
            );
            when(userService.getCurrentUser()).thenReturn(coordinator);
            when(
                gradableEventService.getGradableEventById(assignment.getId())
            ).thenReturn(assignment);
            when(
                studentRepository.findByUserIdAndGradableEventId(
                    student.getUserId(),
                    assignment.getId()
                )
            ).thenReturn(Optional.of(student));
            when(
                submissionRequirementRepository.getSubmissionRequirementsByGradableEvent(
                    assignment
                )
            ).thenReturn(
                List.of(existingSubmission.getSubmissionRequirement())
            );
            when(
                submissionRepository.getSubmissionsByGradableEventAndStudent(
                    assignment.getId(),
                    student.getUserId()
                )
            ).thenReturn(List.of(existingSubmission));

            submissionService.putSubmissionDetails(
                assignment.getId(),
                requestDto
            );

            verify(submissionRepository).saveAll(
                saveSubmissionCaptor.capture()
            );
            verify(submissionRepository).deleteAll(
                deleteSubmissionCaptor.capture()
            );
            verify(gradableEventService).validateTargetGradableEventAccess(target, assignment);
            List<Submission> savedSubmissions = saveSubmissionCaptor.getValue();
            assertThat(savedSubmissions).hasSize(1);
            Submission savedSubmission = savedSubmissions.getFirst();
            assertThat(savedSubmission.getUrl()).isEqualTo(
                existingSubmission.getUrl()
            );
            assertThat(savedSubmission.isLocked()).isTrue();
            assertThat(savedSubmission.getAnimal().getId()).isEqualTo(1L);
            List<Submission> deletedSubmissions =
                deleteSubmissionCaptor.getValue();
            assertThat(deletedSubmissions).isEmpty();
        }

        @Test
        void putSubmissionDetails_whenStudentTargetsOtherStudent_shouldThrowException() {
            Student currentUser = Student.builder().build();
            currentUser.setUserId(1L);
            long otherStudentId = 2L;
            TargetRequestDto target = new StudentTargetRequestDto(
                otherStudentId
            );
            SubmissionDetailsRequestDto requestDto =
                new SubmissionDetailsRequestDto(target, Map.of());

            when(userService.getCurrentUserRole()).thenReturn(UserType.STUDENT);
            when(userService.getCurrentUser()).thenReturn(currentUser);
            when(
                gradableEventService.getGradableEventById(assignment.getId())
            ).thenReturn(assignment);

            ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () ->
                    submissionService.putSubmissionDetails(
                        assignment.getId(),
                        requestDto
                    )
            );
            assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
            assertEquals("Nie znaleziono studenta.", ex.getReason());
        }
    }

    @Nested
    class ProjectGroupTests {

        @Test
        void getSubmissionDetails_whenStudentInProjectGroup_shouldReturnDetails() {
            Student student = Student.builder().build();
            student.setUserId(1L);
            var animal = Animal.builder()
                .id(1L)
                .studentCourseGroupAssignment(
                    new StudentCourseGroupAssignment(null, student, null, null)
                )
                .build();
            ProjectGroup projectGroup = ProjectGroup.builder()
                .id(1L)
                .animals(List.of(animal))
                .build();
            TargetRequestDto target = new StudentGroupTargetRequestDto(
                projectGroup.getId()
            );

            List<SubmissionRequirement> requirements = List.of(
                SubmissionRequirement.builder().id(1L).build()
            );
            List<Submission> submissions = List.of(
                Submission.builder()
                    .submissionRequirement(requirements.getFirst())
                    .url("http://project.com")
                    .build()
            );

            when(userService.getCurrentUserRole()).thenReturn(UserType.STUDENT);
            when(userService.getCurrentUser()).thenReturn(student);
            when(
                gradableEventService.getGradableEventById(project.getId())
            ).thenReturn(project);
            when(
                projectGroupRepository.getProjectGroupByIdAndStudentIdAndProjectId(
                    projectGroup.getId(),
                    student.getUserId(),
                    project.getId()
                )
            ).thenReturn(Optional.of(projectGroup));
            when(
                    projectGroupRepository.getStudentsByProjectGroup(projectGroup)
            ).thenReturn(List.of(student));
            when(
                submissionRequirementRepository.getSubmissionRequirementsByGradableEvent(
                    project
                )
            ).thenReturn(requirements);
            when(
                submissionRepository.getSubmissionsByGradableEventAndStudent(
                    anyLong(),
                    anyLong()
                )
            ).thenReturn(submissions);

            Map<Long, SubmissionDetailsDto> result =
                submissionService.getSubmissionDetails(project.getId(), target).details();

            assertThat(result).isNotNull();
            assertThat(result).hasSize(1);
            assertThat(result.get(1L).url()).isEqualTo("http://project.com");
        }

        @Test
        void putSubmissionDetails_whenStudentInProjectGroup_shouldUpdateForAllStudentsInGroup() {
            Student student1 = Student.builder().build();
            student1.setUserId(1L);
            Student student2 = Student.builder().build();
            student2.setUserId(2L);
            var animal1 = Animal.builder()
                .id(10L)
                .studentCourseGroupAssignment(
                    new StudentCourseGroupAssignment(null, student1, null, null)
                )
                .build();
            var animal2 = Animal.builder()
                .id(11L)
                .studentCourseGroupAssignment(
                    new StudentCourseGroupAssignment(null, student2, null, null)
                )
                .build();
            ProjectGroup projectGroup = ProjectGroup.builder()
                .id(1L)
                .animals(List.of(animal1, animal2))
                .build();
            TargetRequestDto target = new StudentGroupTargetRequestDto(
                projectGroup.getId()
            );

            long requirementId = 1L;
            SubmissionDetailsDto detailsDto = SubmissionDetailsDto.builder()
                .url("http://project-new.com")
                .isLocked(false)
                .build();
            SubmissionDetailsRequestDto requestDto =
                new SubmissionDetailsRequestDto(
                    target,
                    Map.of(requirementId, detailsDto)
                );
            SubmissionRequirement requirement = SubmissionRequirement.builder()
                .id(requirementId)
                .build();

            when(userService.getCurrentUserRole()).thenReturn(UserType.STUDENT);
            when(userService.getCurrentUser()).thenReturn(student1);
            when(
                gradableEventService.getGradableEventById(project.getId())
            ).thenReturn(project);
            when(
                projectGroupRepository.getProjectGroupByIdAndStudentIdAndProjectId(
                    projectGroup.getId(),
                    student1.getUserId(),
                    project.getId()
                )
            ).thenReturn(Optional.of(projectGroup));
            when(
                    projectGroupRepository.getStudentsByProjectGroup(projectGroup)
            ).thenReturn(List.of(student1, student2));
            when(
                submissionRequirementRepository.getSubmissionRequirementsByGradableEvent(
                    project
                )
            ).thenReturn(List.of(requirement));
            when(
                submissionRepository.getSubmissionsByGradableEventAndStudent(
                    project.getId(),
                    student1.getUserId()
                )
            ).thenReturn(Collections.emptyList());
            when(
                submissionRepository.getSubmissionsByGradableEventAndStudent(
                    project.getId(),
                    student2.getUserId()
                )
            ).thenReturn(Collections.emptyList());
            when(
                animalService.getAnimal(student1.getUserId(), course.getId())
            ).thenReturn(animal1);
            when(
                animalService.getAnimal(student2.getUserId(), course.getId())
            ).thenReturn(animal2);

            submissionService.putSubmissionDetails(project.getId(), requestDto);

            verify(submissionRepository).saveAll(
                saveSubmissionCaptor.capture()
            );
            List<Submission> savedSubmissions = saveSubmissionCaptor.getValue();
            assertThat(savedSubmissions).hasSize(2);
            assertThat(savedSubmissions)
                .extracting(Submission::getUrl)
                .containsOnly("http://project-new.com");
            assertThat(savedSubmissions)
                .extracting(Submission::getAnimal)
                .extracting(Animal::getId)
                .containsExactlyInAnyOrder(animal1.getId(), animal2.getId());
        }

        @Test
        void putSubmissionDetails_whenInstructorTargetsProjectGroup_shouldUpdateForAllStudentsInGroup() {
            Instructor instructor = Instructor.builder().build();
            instructor.setUserId(99L);
            Student student1 = Student.builder().build();
            student1.setUserId(1L);
            Student student2 = Student.builder().build();
            student2.setUserId(2L);
            var animal1 = Animal.builder()
                .id(10L)
                .studentCourseGroupAssignment(
                    new StudentCourseGroupAssignment(null, student1, null, null)
                )
                .build();
            var animal2 = Animal.builder()
                .id(11L)
                .studentCourseGroupAssignment(
                    new StudentCourseGroupAssignment(null, student2, null, null)
                )
                .build();
            ProjectGroup projectGroup = ProjectGroup.builder()
                .id(1L)
                .animals(List.of(animal1, animal2))
                .build();
            TargetRequestDto target = new StudentGroupTargetRequestDto(
                projectGroup.getId()
            );

            long requirementId = 1L;
            SubmissionDetailsDto detailsDto = SubmissionDetailsDto.builder()
                .url("http://project-instr.com")
                .isLocked(true)
                .build();
            SubmissionDetailsRequestDto requestDto =
                new SubmissionDetailsRequestDto(
                    target,
                    Map.of(requirementId, detailsDto)
                );
            SubmissionRequirement requirement = SubmissionRequirement.builder()
                .id(requirementId)
                .build();

            when(userService.getCurrentUserRole()).thenReturn(
                UserType.INSTRUCTOR
            );
            when(userService.getCurrentUser()).thenReturn(instructor);
            when(
                gradableEventService.getGradableEventById(project.getId())
            ).thenReturn(project);
            when(
                projectGroupRepository.getProjectGroupByIdAndProjectIdAndTeachingRoleUserId(
                    projectGroup.getId(),
                    project.getId(),
                    instructor.getUserId()
                )
            ).thenReturn(Optional.of(projectGroup));
            when(
                    projectGroupRepository.getStudentsByProjectGroup(projectGroup)
            ).thenReturn(List.of(student1, student2));
            when(
                submissionRequirementRepository.getSubmissionRequirementsByGradableEvent(
                    project
                )
            ).thenReturn(List.of(requirement));
            when(
                submissionRepository.getSubmissionsByGradableEventAndStudent(
                    project.getId(),
                    student1.getUserId()
                )
            ).thenReturn(Collections.emptyList());
            when(
                submissionRepository.getSubmissionsByGradableEventAndStudent(
                    project.getId(),
                    student2.getUserId()
                )
            ).thenReturn(Collections.emptyList());
            when(
                animalService.getAnimal(student1.getUserId(), course.getId())
            ).thenReturn(animal1);
            when(
                animalService.getAnimal(student2.getUserId(), course.getId())
            ).thenReturn(animal2);

            submissionService.putSubmissionDetails(project.getId(), requestDto);

            verify(submissionRepository).saveAll(
                saveSubmissionCaptor.capture()
            );
            List<Submission> savedSubmissions = saveSubmissionCaptor.getValue();
            assertThat(savedSubmissions).hasSize(2);
            assertThat(savedSubmissions)
                .extracting(Submission::getUrl)
                .containsOnly("http://project-instr.com");
            assertThat(savedSubmissions)
                .extracting(Submission::isLocked)
                .containsOnly(true);
            assertThat(savedSubmissions)
                .extracting(Submission::getAnimal)
                .extracting(Animal::getId)
                .containsExactlyInAnyOrder(animal1.getId(), animal2.getId());
        }

        @Test
        void putSubmissionDetails_whenStudentTargetAndProject_shouldUpdateForAllGroupMembers() {
            Student student1 = Student.builder().build();
            student1.setUserId(1L);
            Student student2 = Student.builder().build();
            student2.setUserId(2L);
            var animal1 = Animal.builder()
                .id(10L)
                .studentCourseGroupAssignment(
                    new StudentCourseGroupAssignment(null, student1, null, null)
                )
                .build();
            var animal2 = Animal.builder()
                .id(11L)
                .studentCourseGroupAssignment(
                    new StudentCourseGroupAssignment(null, student2, null, null)
                )
                .build();
            ProjectGroup projectGroup = ProjectGroup.builder()
                .id(1L)
                .animals(List.of(animal1, animal2))
                .build();
            TargetRequestDto target = new StudentTargetRequestDto(
                student1.getUserId()
            );

            long requirementId = 1L;
            SubmissionDetailsDto detailsDto = SubmissionDetailsDto.builder()
                .url("http://project-new.com")
                .isLocked(false)
                .build();
            SubmissionDetailsRequestDto requestDto =
                new SubmissionDetailsRequestDto(
                    target,
                    Map.of(requirementId, detailsDto)
                );
            SubmissionRequirement requirement = SubmissionRequirement.builder()
                .id(requirementId)
                .build();

            when(userService.getCurrentUserRole()).thenReturn(UserType.STUDENT);
            when(userService.getCurrentUser()).thenReturn(student1);
            when(
                gradableEventService.getGradableEventById(project.getId())
            ).thenReturn(project);
            when(
                projectGroupRepository.getProjectGroupByStudentIdAndProjectId(
                    student1.getUserId(),
                    project.getId()
                )
            ).thenReturn(Optional.of(projectGroup));
            when(
                    projectGroupRepository.getStudentsByProjectGroup(projectGroup)
            ).thenReturn(List.of(student1, student2));
            when(
                submissionRequirementRepository.getSubmissionRequirementsByGradableEvent(
                    project
                )
            ).thenReturn(List.of(requirement));
            when(
                submissionRepository.getSubmissionsByGradableEventAndStudent(
                    anyLong(),
                    anyLong()
                )
            ).thenReturn(Collections.emptyList());
            when(
                animalService.getAnimal(student1.getUserId(), course.getId())
            ).thenReturn(animal1);
            when(
                animalService.getAnimal(student2.getUserId(), course.getId())
            ).thenReturn(animal2);

            submissionService.putSubmissionDetails(project.getId(), requestDto);

            verify(submissionRepository).saveAll(
                saveSubmissionCaptor.capture()
            );
            List<Submission> savedSubmissions = saveSubmissionCaptor.getValue();
            assertThat(savedSubmissions).hasSize(2);
            assertThat(savedSubmissions)
                .extracting(Submission::getAnimal)
                .extracting(Animal::getId)
                .containsExactlyInAnyOrder(animal1.getId(), animal2.getId());
        }

        @Test
        void putSubmissionDetails_whenInstructorTargetsStudentInProject_shouldUpdateForAllGroupMembers() {
            Instructor instructor = Instructor.builder().build();
            instructor.setUserId(99L);
            Student student1 = Student.builder().build();
            student1.setUserId(1L);
            Student student2 = Student.builder().build();
            student2.setUserId(2L);
            var animal1 = Animal.builder()
                .id(10L)
                .studentCourseGroupAssignment(
                    new StudentCourseGroupAssignment(null, student1, null, null)
                )
                .build();
            var animal2 = Animal.builder()
                .id(11L)
                .studentCourseGroupAssignment(
                    new StudentCourseGroupAssignment(null, student2, null, null)
                )
                .build();
            ProjectGroup projectGroup = ProjectGroup.builder()
                .id(1L)
                .animals(List.of(animal1, animal2))
                .build();
            TargetRequestDto target = new StudentTargetRequestDto(
                student1.getUserId()
            );

            long requirementId = 1L;
            SubmissionDetailsDto detailsDto = SubmissionDetailsDto.builder()
                .url("http://project-instr.com")
                .isLocked(true)
                .build();
            SubmissionDetailsRequestDto requestDto =
                new SubmissionDetailsRequestDto(
                    target,
                    Map.of(requirementId, detailsDto)
                );
            SubmissionRequirement requirement = SubmissionRequirement.builder()
                .id(requirementId)
                .build();

            when(userService.getCurrentUserRole()).thenReturn(
                UserType.INSTRUCTOR
            );
            when(userService.getCurrentUser()).thenReturn(instructor);
            when(
                gradableEventService.getGradableEventById(project.getId())
            ).thenReturn(project);
            when(
                projectGroupRepository.getProjectGroupByStudentIdAndProjectIdAndTeachingRoleUserId(
                    student1.getUserId(),
                    project.getId(),
                    instructor.getUserId()
                )
            ).thenReturn(Optional.of(projectGroup));
            when(
                    projectGroupRepository.getStudentsByProjectGroup(projectGroup)
            ).thenReturn(List.of(student1, student2));
            when(
                submissionRequirementRepository.getSubmissionRequirementsByGradableEvent(
                    project
                )
            ).thenReturn(List.of(requirement));
            when(
                submissionRepository.getSubmissionsByGradableEventAndStudent(
                    anyLong(),
                    anyLong()
                )
            ).thenReturn(Collections.emptyList());
            when(
                animalService.getAnimal(student1.getUserId(), course.getId())
            ).thenReturn(animal1);
            when(
                animalService.getAnimal(student2.getUserId(), course.getId())
            ).thenReturn(animal2);

            submissionService.putSubmissionDetails(project.getId(), requestDto);

            verify(submissionRepository).saveAll(
                saveSubmissionCaptor.capture()
            );
            List<Submission> savedSubmissions = saveSubmissionCaptor.getValue();
            assertThat(savedSubmissions).hasSize(2);
            assertThat(savedSubmissions)
                .extracting(Submission::getAnimal)
                .extracting(Animal::getId)
                .containsExactlyInAnyOrder(animal1.getId(), animal2.getId());
            assertThat(savedSubmissions)
                .extracting(Submission::isLocked)
                .containsOnly(true);
        }
    }
}
