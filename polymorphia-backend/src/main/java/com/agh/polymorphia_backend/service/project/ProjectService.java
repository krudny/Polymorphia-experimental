package com.agh.polymorphia_backend.service.project;

import com.agh.polymorphia_backend.dto.request.project.ProjectGroupConfigurationRequestDto;
import com.agh.polymorphia_backend.dto.request.project.ProjectGroupPickStudentsRequestDto;
import com.agh.polymorphia_backend.dto.request.project.ProjectGroupUpdateRequestDto;
import com.agh.polymorphia_backend.dto.request.target.StudentGroupTargetRequestDto;
import com.agh.polymorphia_backend.dto.request.target.StudentTargetRequestDto;
import com.agh.polymorphia_backend.dto.request.target.TargetRequestDto;
import com.agh.polymorphia_backend.dto.request.target.TargetType;
import com.agh.polymorphia_backend.dto.response.project.ProjectCategoryWithVariantsResponseDto;
import com.agh.polymorphia_backend.dto.response.project.ProjectGroupConfigurationResponseDto;
import com.agh.polymorphia_backend.dto.response.project.ProjectGroupPickStudentsResponseDto;
import com.agh.polymorphia_backend.dto.response.project.ProjectVariantResponseDto;
import com.agh.polymorphia_backend.dto.response.project.filter.FilterOptionResponseDto;
import com.agh.polymorphia_backend.dto.response.project.filter.ProjectGroupConfigurationFiltersResponseDto;
import com.agh.polymorphia_backend.dto.response.user_context.UserDetailsResponseDto;
import com.agh.polymorphia_backend.model.gradable_event.subtypes.project.Project;
import com.agh.polymorphia_backend.model.project.ProjectGroup;
import com.agh.polymorphia_backend.model.project.ProjectVariant;
import com.agh.polymorphia_backend.model.project.ProjectVariantCategory;
import com.agh.polymorphia_backend.model.user.TeachingRoleUser;
import com.agh.polymorphia_backend.model.user.UserType;
import com.agh.polymorphia_backend.model.user.student.Animal;
import com.agh.polymorphia_backend.repository.course.CourseGroupRepository;
import com.agh.polymorphia_backend.repository.project.ProjectRepository;
import com.agh.polymorphia_backend.repository.project.ProjectVariantRepository;
import com.agh.polymorphia_backend.repository.project.SuggestedVariant;
import com.agh.polymorphia_backend.service.mapper.ProjectMapper;
import com.agh.polymorphia_backend.service.student.AnimalService;
import com.agh.polymorphia_backend.service.user.UserService;
import com.agh.polymorphia_backend.service.validation.AccessAuthorizer;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

@Service
@AllArgsConstructor
public class ProjectService {
    private static final int PER_CATEGORY_LIMIT = 2;
    public static final String ANIMAL_IF_NOT_ASSIGNED_TO_ANY_PROJECT_GROUP = "Zwierzak nie jest przypisany do żadnej grupy projektowej.";
    private final ProjectRepository projectRepository;
    private final CourseGroupRepository courseGroupRepository;
    private final ProjectVariantRepository projectVariantRepository;
    private final AccessAuthorizer accessAuthorizer;
    private final AnimalService animalService;
    private final UserService userService;
    private final ProjectMapper projectMapper;
    private final ProjectGroupService projectGroupService;

    public List<ProjectVariantResponseDto> getProjectVariants(TargetRequestDto target, Long projectId) {
        if (userService.getCurrentUserRole().equals(UserType.STUDENT)){
            return projectGroupService.findByProjectIdAndStudentId(projectId, ((StudentTargetRequestDto)target).id())
                    .map(projectGroup -> {
                        accessAuthorizer.authorizeProjectGroupDetailsAccess(projectGroup.getId());
                        return projectGroup.getProjectVariants();
                    })
                    .orElseGet(Collections::emptyList).stream()
                    .map(projectMapper::toProjectVariantResponseDto)
                    .toList();
        }
        return getProjectGroupForTargetAndAuthorize(projectId, target).getProjectVariants().stream()
                .map(projectMapper::toProjectVariantResponseDto)
                .toList();
    }

    public Map<Long, Long> getSuggestedProjectVariants(Optional<TargetRequestDto> target, Long projectId) {
        Optional<ProjectGroup> projectGroup = getProjectGroupForTargetAndAuthorize(
                projectId,
                projectRepository.getCourseIdByProjectId(projectId),
                target
        );
        Long groupId = projectGroup.map(ProjectGroup::getId).orElse(null);

        List<SuggestedVariant> suggestedVariants = projectRepository.suggestVariantsForNewGroup(projectId, groupId, PER_CATEGORY_LIMIT);

        return suggestedVariants.stream()
                .collect(toMap(
                        SuggestedVariant::categoryId,
                        SuggestedVariant::variantId
                ));
    }

    public List<UserDetailsResponseDto> getProjectGroup(Long projectGroupId) {
        accessAuthorizer.authorizeProjectGroupDetailsAccess(projectGroupId);
        return projectGroupService.getUserDetailsResponseByProjectGroup(projectGroupId);
    }

    public List<UserDetailsResponseDto> getAnimalProjectGroup(Long userId, Long projectId) {
        return projectGroupService.findByProjectIdAndStudentId(projectId, userId)
                .map(projectGroup -> {
                    accessAuthorizer.authorizeProjectGroupDetailsAccess(projectGroup.getId());
                    return projectGroupService.getUserDetailsResponseByProjectGroup(projectGroup.getId());
                })
                .orElseGet(() -> {
                    if (userService.getCurrentUserRole().equals(UserType.STUDENT)) {
                        return Collections.emptyList();
                    }
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, ANIMAL_IF_NOT_ASSIGNED_TO_ANY_PROJECT_GROUP);
                });
    }

    public ProjectGroupConfigurationResponseDto getProjectGroupConfiguration(TargetRequestDto targetRequestDto, Long projectId) {
        ProjectGroup projectGroup = getProjectGroupForTargetAndAuthorize(projectId, targetRequestDto);
        List<Long> studentIds = projectGroupService.getStudentIdsFromProjectGroup(projectGroup.getId());

        Map<Long, Long> selectedVariants = projectGroup.getProjectVariants().stream()
                .collect(toMap(
                        projectVariant -> projectVariant.getCategory().getId(),
                        ProjectVariant::getId
                ));

        return ProjectGroupConfigurationResponseDto.builder()
                .studentIds(studentIds)
                .selectedVariants(selectedVariants)
                .build();
    }

    @Transactional
    public void deleteProjectGroup(TargetRequestDto targetRequestDto, Long projectId) {
        ProjectGroup projectGroup = getProjectGroupForTargetAndAuthorize(projectId, targetRequestDto);
        projectGroupService.delete(projectGroup);
    }

    @Transactional
    public void processProjectGroupConfiguration(ProjectGroupConfigurationRequestDto requestDto, Long projectId) {
        ProjectGroupUpdateRequestDto projectGroupUpdate = requestDto.getProjectGroupUpdate();
        Project project = getProjectGradableEvent(projectId);
        Long courseId = projectRepository.getCourseIdByProjectId(projectId);
        Optional<ProjectGroup> projectGroupOptional = getProjectGroupForTargetAndAuthorize(projectId, courseId, Optional.ofNullable(requestDto.getTarget()));
        List<ProjectVariant> projectVariants = getAndValidateSelectedVariants(project, projectGroupUpdate.getSelectedVariants());
        List<Animal> animals = getAndValidateAnimals(projectGroupUpdate.getStudentIds(), courseId, project.isAllowCrossCourseGroupProjectGroups());
        ProjectGroup projectGroup = projectGroupOptional.orElseGet(() -> {
            ProjectGroup pg = new ProjectGroup();
            pg.setTeachingRoleUser((TeachingRoleUser) userService.getCurrentUser());
            pg.setProject(project);
            return pg;
        });
        projectGroup.setProjectVariants(projectVariants);
        projectGroup.setAnimals(animals);
        projectGroupService.save(projectGroup);
    }

    private List<Animal> getAndValidateAnimals(List<Long> studentIds, Long courseId, boolean allowCrossCourseGroupProjectGroups) {
        List<Animal> animals = animalService.getAnimals(studentIds, courseId);
        if (!allowCrossCourseGroupProjectGroups) {
            if (animalService.countAnimalCourseGroups(animals) > 1) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Przypisanie studentów z różnych grup zajęciowych do jednej grupy projektowej jest niemożliwe.");
            }
            studentIds.forEach(studentId -> accessAuthorizer.authorizeStudentDataAccess(courseId, studentId));
        }
        return animals;
    }


    private List<ProjectVariant> getAndValidateSelectedVariants(Project project, Map<Long, Long> selectedVariants) {
        Set<Long> requiredCategoriesIds = project.getVariantCategories().stream()
                .map(ProjectVariantCategory::getId).collect(Collectors.toSet());

        if (!requiredCategoriesIds.equals(selectedVariants.keySet())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Przekazane kategorie wariantów nie pokrywają się z kategoriami projektu.");
        }

        List<ProjectVariant> variants = projectVariantRepository
                .findAllById(selectedVariants.values());

        Map<Long, ProjectVariant> variantsById = variants.stream()
                .collect(Collectors.toMap(ProjectVariant::getId, Function.identity()));

        for (Map.Entry<Long, Long> entry : selectedVariants.entrySet()) {
            ProjectVariant variant = variantsById.get(entry.getValue());
            if (variant == null ||
                    !variant.getCategory().getId().equals(entry.getKey())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wariant nie istnieje lub nie należy do podanej kategorii.");
            }
        }

        return variants;
    }


    public ProjectGroupConfigurationFiltersResponseDto getProjectGroupConfigurationFilters(Optional<TargetRequestDto> targetRequestDto, Long projectId) {
        Project project = getProjectGradableEvent(projectId);
        Long courseId = projectRepository.getCourseIdByProjectId(projectId);
        getProjectGroupForTargetAndAuthorize(project.getId(), courseId, targetRequestDto);

        Long teachingRoleUserId = project.isAllowCrossCourseGroupProjectGroups() ? null : userService.getCurrentUser().getUser().getId();
        List<FilterOptionResponseDto> options = courseGroupRepository
                .findShortCourseGroups(courseId, null, teachingRoleUserId)
                .stream()
                .map(courseGroup -> FilterOptionResponseDto.builder()
                        .value(courseGroup.getName())
                        .build())
                .collect(Collectors.toList());

        ProjectGroupConfigurationFiltersResponseDto.ProjectGroupConfigurationFiltersResponseDtoBuilder builder = ProjectGroupConfigurationFiltersResponseDto.builder();
        if (project.isAllowCrossCourseGroupProjectGroups()) {
            options.addFirst(FilterOptionResponseDto.builder()
                    .value("all")
                    .label("Wszystkie")
                    .specialBehavior("EXCLUSIVE")
                    .build());
            builder.max((long) options.size() - 1)
                    .options(options)
                    .defaultValues(List.of("all"))
                    .additionalInfo("Grupy projektowe mogą obejmować studentów z różnych grup zajęciowych.");
        } else {
            builder.max(1L)
                    .options(options)
                    .defaultValues(List.of(options.getFirst().getValue()))
                    .additionalInfo("Grupy projektowe są ograniczone do studentów z jednej grupy zajęciowej.");
        }

        return builder.build();
    }

    public List<ProjectGroupPickStudentsResponseDto> getProjectGroupConfigurationGroupPickStudents(ProjectGroupPickStudentsRequestDto requestDto, Long projectId) {
        boolean includeAllGroups = requestDto.getGroups().size() == 1 && requestDto.getGroups().getFirst().equals("all");
        Project project = getProjectGradableEvent(projectId);
        Long courseId = projectRepository.getCourseIdByProjectId(projectId);
        Long userId = userService.getCurrentUser().getUser().getId();
        List<ProjectGroupPickStudentsResponseDto> students = projectRepository
                .getProjectGroupConfigurationGroupPickStudents(courseId, project, userId, requestDto.getGroups(), includeAllGroups);

        getProjectGroupForTargetAndAuthorize(projectId, courseId, Optional.ofNullable(requestDto.getTarget()))
                .ifPresent(projectGroup -> students.addAll(projectRepository.getProjectGroupConfigurationGroupPickStudents(projectGroup)));
        return students;
    }

    public List<ProjectCategoryWithVariantsResponseDto> getProjectCategoryWithVariants(Long projectId) {
        accessAuthorizer.authorizeCurrentUserCourseAccess(projectRepository.getCourseIdByProjectId(projectId));

        return projectRepository.findCategoriesWithVariants(projectId)
                .stream()
                .map(projectMapper::toProjectCategoryResponseDto)
                .toList();
    }

    private ProjectGroup getProjectGroupForTargetAndAuthorize(Long projectId, TargetRequestDto target) {
        ProjectGroup projectGroup = target.type().equals(TargetType.STUDENT) ?
                projectGroupService.findByProjectIdAndStudentId(projectId, ((StudentTargetRequestDto) target).id())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ANIMAL_IF_NOT_ASSIGNED_TO_ANY_PROJECT_GROUP)) :
                projectGroupService.findById(((StudentGroupTargetRequestDto) target).groupId());
        accessAuthorizer.authorizeProjectGroupDetailsAccess(projectGroup.getId());
        return projectGroup;
    }

    private Optional<ProjectGroup> getProjectGroupForTargetAndAuthorize(Long projectId, Long courseId, Optional<TargetRequestDto> target) {
        if (target.isPresent()) {
            return Optional.of(getProjectGroupForTargetAndAuthorize(projectId, target.get()));
        } else {
            accessAuthorizer.authorizeCurrentUserCourseAccess(courseId);
            return Optional.empty();
        }
    }

    private Project getProjectGradableEvent(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Projekt nie został znaleziony."));
    }
}
