package com.agh.polymorphia_backend.service.course;

import com.agh.polymorphia_backend.dto.request.course_import.CourseDetailsRequestDto;
import com.agh.polymorphia_backend.dto.request.course_import.EvolutionStageDetailsRequestDto;
import com.agh.polymorphia_backend.dto.request.course_import.SubmissionRequirementDetailsRequestDto;
import com.agh.polymorphia_backend.dto.request.course_import.criterion.CriterionDetailsRequestDto;
import com.agh.polymorphia_backend.dto.request.course_import.criterion.CriterionRewardDetailsRequestDto;
import com.agh.polymorphia_backend.dto.request.course_import.event_section.EventSectionDetailsRequestDto;
import com.agh.polymorphia_backend.dto.request.course_import.gradable_event.AssignmentDetailsRequestDto;
import com.agh.polymorphia_backend.dto.request.course_import.gradable_event.GradableEventDetailsRequestDto;
import com.agh.polymorphia_backend.dto.request.course_import.gradable_event.ProjectDetailsRequestDto;
import com.agh.polymorphia_backend.dto.request.course_import.reward.ChestDetailsRequestDto;
import com.agh.polymorphia_backend.dto.request.course_import.reward.ItemDetailsRequestDto;
import com.agh.polymorphia_backend.dto.request.course_import.variant.VariantCategoryDetailsRequestDto;
import com.agh.polymorphia_backend.dto.request.course_import.variant.VariantDetailsRequestDto;
import com.agh.polymorphia_backend.model.course.Course;
import com.agh.polymorphia_backend.model.criterion.CriterionReward;
import com.agh.polymorphia_backend.model.event_section.EventSectionType;
import com.agh.polymorphia_backend.model.gradable_event.GradableEvent;
import com.agh.polymorphia_backend.model.reward.Reward;
import com.agh.polymorphia_backend.model.user.User;
import com.agh.polymorphia_backend.model.user.UserCourseRole;
import com.agh.polymorphia_backend.model.user.UserCourseRoleId;
import com.agh.polymorphia_backend.model.user.UserType;
import com.agh.polymorphia_backend.model.user.coordinator.Coordinator;
import com.agh.polymorphia_backend.repository.course.CourseRepository;
import com.agh.polymorphia_backend.repository.criterion.CriterionRepository;
import com.agh.polymorphia_backend.repository.criterion.CriterionRewardRepository;
import com.agh.polymorphia_backend.repository.event_section.EventSectionRepository;
import com.agh.polymorphia_backend.repository.gradable_event.GradableEventRepository;
import com.agh.polymorphia_backend.repository.gradable_event.projections.GradableEventDetailsProjection;
import com.agh.polymorphia_backend.repository.project.ProjectVariantCategoryRepository;
import com.agh.polymorphia_backend.repository.reward.RewardRepository;
import com.agh.polymorphia_backend.repository.user.UserCourseRoleRepository;
import com.agh.polymorphia_backend.repository.user.role.CoordinatorRepository;
import com.agh.polymorphia_backend.service.course.strategy.*;
import com.agh.polymorphia_backend.service.user.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CourseImportService {

    private final CourseService courseService;
    private final CourseDetailsService courseDetailsService;
    private final EvolutionStageUpdateStrategy evolutionStageUpdateStrategy;
    private final ItemUpdateStrategy itemUpdateStrategy;
    private final ChestUpdateStrategy chestUpdateStrategy;
    private final EventSectionUpdateStrategy eventSectionUpdateStrategy;
    private final GradableEventUpdateStrategy gradableEventUpdateStrategy;
    private final EventSectionRepository eventSectionRepository;
    private final CriterionUpdateStrategy criterionUpdateStrategy;
    private final GradableEventRepository gradableEventRepository;
    private final SubmissionRequirementUpdateStrategy submissionRequirementUpdateStrategy;
    private final ProjectVariantCategoryUpdateStrategy projectVariantCategoryUpdateStrategy;
    private final ProjectVariantUpdateStrategy projectVariantUpdateStrategy;
    private final ProjectVariantCategoryRepository projectVariantCategoryRepository;
    private final CriterionRepository criterionRepository;
    private final RewardRepository rewardRepository;
    private final CriterionRewardRepository criterionRewardRepository;
    private final CourseRepository courseRepository;
    private final UserService userService;
    private final UserCourseRoleRepository userCourseRoleRepository;
    private final CoordinatorRepository coordinatorRepository;

    @Transactional
    public void importCourse(CourseDetailsRequestDto request) {
        User currentUser = userService.getCurrentUser().getUser();
        importCourseForUser(request, currentUser);
    }

    @Transactional
    public void importCourseForUser(CourseDetailsRequestDto request, User currentUser) {
        validateUniqueKeys(request);
        Coordinator coordinator = getOrCreateCoordinator(currentUser);

        Course course = Course.builder()
                .coordinator(coordinator)
                .build();
        updateCourseDetails(request, course);
        Course savedCourse = courseRepository.save(course);
        courseRepository.flush();
        Long courseId = savedCourse.getId();

        UserCourseRole userCourseRole = UserCourseRole.builder()
                .course(savedCourse)
                .user(currentUser)
                .id(new UserCourseRoleId(currentUser.getId(), courseId))
                .role(UserType.COORDINATOR)
                .build();
        userCourseRoleRepository.save(userCourseRole);

        updateCourseElements(request, new CourseDetailsRequestDto(), courseId);
    }

    @Transactional
    public void updateCourse(CourseDetailsRequestDto request, Long courseId) {
        CourseDetailsRequestDto currentConfig = courseDetailsService.getCourseDetails(courseId);
        validateUniqueKeys(request);

        if (!request.equals(currentConfig)) {
            Course course = courseService.getCourseById(courseId);
            updateCourseDetails(request, course);
            courseRepository.save(course);
        }
        updateCourseElements(request, currentConfig, courseId);
    }

    private Coordinator getOrCreateCoordinator(User user) {
        Coordinator coordinator = coordinatorRepository.findById(user.getId())
                .orElse(Coordinator.builder().user(user).build());
        return coordinatorRepository.save(coordinator);
    }

    private void updateCourseElements(CourseDetailsRequestDto request, CourseDetailsRequestDto currentConfig, Long courseId) {
        updateEvolutionStages(request.getEvolutionStages(), currentConfig.getEvolutionStages(), courseId);
        updateEventSections(request.getEventSections(), currentConfig.getEventSections(), courseId);
        updateItems(request.getItems(), currentConfig.getItems(), courseId);
        updateChests(request.getChests(), currentConfig.getChests(), courseId);
        updateAllCriteriaRewards(request.getEventSections(), courseId);
        updateRoadmapOrder(request.getRoadmapOrderKeys(), courseId);
    }

    private void updateRoadmapOrder(List<String> roadmapOrderKeys, Long courseId) {
        validateRoadmapOrderKeys(roadmapOrderKeys, courseId);
        if (roadmapOrderKeys.isEmpty()) {
            return;
        }
        List<GradableEvent> gradableEvents = gradableEventRepository.findAllByKeyIn(roadmapOrderKeys, courseId);

        Map<String, GradableEvent> eventsByKey = gradableEvents.stream()
                .collect(Collectors.toMap(GradableEvent::getKey, Function.identity()));

        List<GradableEvent> updatedGradableEvents = new ArrayList<>();
        for (int i = 0; i < roadmapOrderKeys.size(); i++) {
            String key = roadmapOrderKeys.get(i);
            GradableEvent gradableEvent = eventsByKey.get(key);
            if (gradableEvent != null) {
                gradableEvent.setRoadMapOrderIndex((long) i);
                updatedGradableEvents.add(gradableEvent);
            }
        }

        gradableEventRepository.saveAll(updatedGradableEvents);
        gradableEventRepository.flush();
    }

    private void validateRoadmapOrderKeys(List<String> roadmapOrderKeys, Long courseId) {
        Set<String> uniqueKeys = new HashSet<>(roadmapOrderKeys);
        if (uniqueKeys.size() != roadmapOrderKeys.size()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Klucze w roadmapOrder nie mogą się powtarzać.");
        }

        List<GradableEventDetailsProjection> gradableEvents = gradableEventRepository.findGradableEventsByCourseId(courseId);
        Set<String> shownInRoadmapKeys = gradableEvents.stream()
                .filter(GradableEventDetailsProjection::getIsShownInRoadmap)
                .map(GradableEventDetailsProjection::getKey)
                .collect(Collectors.toSet());

        boolean hasMissingKeys = !uniqueKeys.containsAll(shownInRoadmapKeys);
        boolean hasItemsNotInRoadmap = !shownInRoadmapKeys.containsAll(uniqueKeys);

        if (hasMissingKeys || hasItemsNotInRoadmap) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Lista roadmapOrderKeys powinna zawierać wszystkie klucze wydarzeń, które należą " +
                            "do kategorii widocznych w roadmapie i nic ponadto.");
        }
    }

    private void validateUniqueKeys(CourseDetailsRequestDto request) {
        List<String> keys = new ArrayList<>();

        keys.addAll(request.getEvolutionStages().stream()
                .map(EvolutionStageDetailsRequestDto::getKey)
                .toList());

        keys.addAll(request.getEventSections().stream()
                .map(EventSectionDetailsRequestDto::getKey)
                .toList());

        keys.addAll(request.getItems().stream()
                .map(ItemDetailsRequestDto::getKey)
                .toList());

        keys.addAll(request.getChests().stream()
                .map(ChestDetailsRequestDto::getKey)
                .toList());

        keys.addAll(request.getEventSections().stream()
                .flatMap(section -> section.getGradableEvents().stream())
                .map(GradableEventDetailsRequestDto::getKey)
                .toList());

        keys.addAll(request.getEventSections().stream()
                .flatMap(section -> section.getGradableEvents().stream())
                .flatMap(gradableEvent -> gradableEvent.getCriteria().stream())
                .map(CriterionDetailsRequestDto::getKey)
                .toList());

        keys.addAll(request.getEventSections().stream()
                .flatMap(eventSection -> eventSection.getGradableEvents().stream())
                .filter(gradableEvent -> gradableEvent.getType().equals(EventSectionType.PROJECT))
                .flatMap(gradableEvent -> ((ProjectDetailsRequestDto) gradableEvent).getVariantCategories().stream())
                .map(VariantCategoryDetailsRequestDto::getKey)
                .toList());

        keys.addAll(request.getEventSections().stream()
                .flatMap(eventSection -> eventSection.getGradableEvents().stream())
                .filter(gradableEvent -> gradableEvent.getType().equals(EventSectionType.PROJECT))
                .flatMap(gradableEvent -> ((ProjectDetailsRequestDto) gradableEvent).getVariantCategories().stream())
                .flatMap(projectVariantCategory -> projectVariantCategory.getVariants().stream())
                .map(VariantDetailsRequestDto::getKey)
                .toList());

        keys.addAll(request.getEventSections().stream()
                .flatMap(eventSection -> eventSection.getGradableEvents().stream())
                .filter(gradableEvent -> gradableEvent.getType().equals(EventSectionType.PROJECT))
                .flatMap(gradableEvent -> ((ProjectDetailsRequestDto) gradableEvent).getSubmissionRequirements().stream())
                .map(SubmissionRequirementDetailsRequestDto::getKey)
                .toList());

        keys.addAll(request.getEventSections().stream()
                .flatMap(eventSection -> eventSection.getGradableEvents().stream())
                .filter(gradableEvent -> gradableEvent.getType().equals(EventSectionType.ASSIGNMENT))
                .flatMap(gradableEvent -> ((AssignmentDetailsRequestDto) gradableEvent).getSubmissionRequirements().stream())
                .map(SubmissionRequirementDetailsRequestDto::getKey)
                .toList());

        Set<String> uniqueKeys = new HashSet<>(keys);

        if (uniqueKeys.size() != keys.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Klucze muszą być unikalne w obrębie kursu.");
        }
    }


    private void updateCourseDetails(CourseDetailsRequestDto request, Course course) {
        course.setName(request.getName());
        course.setMarkdownSourceUrl(request.getMarkdownSourceUrl());
        course.setImageUrl(request.getImageUrl());
        course.setCoordinatorImageUrl(request.getCoordinatorImageUrl());
        course.setInstructorImageUrl(request.getInstructorImageUrl());
    }

    private void updateGradableEvents(List<GradableEventDetailsRequestDto> request, List<GradableEventDetailsRequestDto> currentConfig, Long eventSectionId, Long courseId) {
        updateEntities(request, currentConfig, eventSectionId, gradableEventUpdateStrategy, courseId);
        Map<String, GradableEventDetailsRequestDto> currentConfigByKey = currentConfig.stream().collect(Collectors.toMap(GradableEventDetailsRequestDto::getKey, Function.identity()));

        request.forEach(gradableEvent -> {
            GradableEventDetailsRequestDto currentGradableEvent = currentConfigByKey.get(gradableEvent.getKey());

            updateCriteria(
                    gradableEvent.getCriteria(),
                    currentGradableEvent != null ?
                            currentGradableEvent.getCriteria() : List.of(),
                    gradableEventRepository.findIdByKeyAndCourseId(gradableEvent.getKey(), courseId),
                    courseId
            );
        });

        request.stream().filter(gradableEvent -> gradableEvent.getType().equals(EventSectionType.ASSIGNMENT))
                .forEach(gradableEvent -> updateAssignment(courseId, gradableEvent, currentConfigByKey));

        request.stream().filter(gradableEvent -> gradableEvent.getType().equals(EventSectionType.PROJECT))
                .forEach(gradableEvent -> updateProject(courseId, gradableEvent, currentConfigByKey));
    }

    private void updateAssignment(Long courseId, GradableEventDetailsRequestDto gradableEvent, Map<String, GradableEventDetailsRequestDto> currentConfigByKey) {
        GradableEventDetailsRequestDto currentGradableEvent = currentConfigByKey.get(gradableEvent.getKey());

        updateSubmissionRequirements(
                ((AssignmentDetailsRequestDto) gradableEvent).getSubmissionRequirements(),
                currentGradableEvent != null && currentGradableEvent.getType().equals(EventSectionType.ASSIGNMENT)
                        ? ((AssignmentDetailsRequestDto) currentGradableEvent).getSubmissionRequirements()
                        : List.of(),
                gradableEventRepository.findIdByKeyAndCourseId(gradableEvent.getKey(), courseId),
                courseId
        );
    }

    private void updateProject(Long courseId, GradableEventDetailsRequestDto gradableEvent, Map<String, GradableEventDetailsRequestDto> currentConfigByKey) {
        GradableEventDetailsRequestDto currentGradableEvent = currentConfigByKey.get(gradableEvent.getKey());
        updateProjectVariantCategories(
                ((ProjectDetailsRequestDto) gradableEvent).getVariantCategories(),
                currentGradableEvent != null && currentGradableEvent.getType().equals(EventSectionType.PROJECT) ?
                        ((ProjectDetailsRequestDto) currentGradableEvent).getVariantCategories()
                        : List.of(),
                gradableEventRepository.findIdByKeyAndCourseId(gradableEvent.getKey(), courseId), courseId);

        updateSubmissionRequirements(
                ((ProjectDetailsRequestDto) gradableEvent).getSubmissionRequirements(),
                currentGradableEvent != null && currentGradableEvent.getType().equals(EventSectionType.ASSIGNMENT)
                        ? ((AssignmentDetailsRequestDto) currentGradableEvent).getSubmissionRequirements()
                        : List.of(),
                gradableEventRepository.findIdByKeyAndCourseId(gradableEvent.getKey(), courseId),
                courseId
        );
    }

    private void updateProjectVariantCategories(List<VariantCategoryDetailsRequestDto> request, List<VariantCategoryDetailsRequestDto> currentConfig, Long gradableEventId, Long courseId) {
        updateEntities(request, currentConfig, gradableEventId, projectVariantCategoryUpdateStrategy, courseId);
        Map<String, VariantCategoryDetailsRequestDto> currentConfigByKey = currentConfig.stream().collect(Collectors.toMap(VariantCategoryDetailsRequestDto::getKey, Function.identity()));

        request.forEach(variantCategory -> {
            VariantCategoryDetailsRequestDto currentVariantCategory = currentConfigByKey.get(variantCategory.getKey());

            updateProjectVariants(
                    variantCategory.getVariants(),
                    currentVariantCategory != null ? currentVariantCategory.getVariants() : List.of(),
                    projectVariantCategoryRepository.findIdByKeyAndCourseId(variantCategory.getKey(), courseId),
                    courseId
            );
        });
    }

    private void updateProjectVariants(List<VariantDetailsRequestDto> request, List<VariantDetailsRequestDto> currentConfig, Long variantCategoryId, Long courseId) {
        updateEntities(request, currentConfig, variantCategoryId, projectVariantUpdateStrategy, courseId);
    }

    private void updateSubmissionRequirements(List<SubmissionRequirementDetailsRequestDto> request, List<SubmissionRequirementDetailsRequestDto> currentConfig, Long gradableEventId, Long courseId) {
        updateEntities(request, currentConfig, gradableEventId, submissionRequirementUpdateStrategy, courseId);
    }

    private void updateCriteria(List<CriterionDetailsRequestDto> request, List<CriterionDetailsRequestDto> currentConfig, Long gradableEventId, Long courseId) {
        updateEntities(request, currentConfig, gradableEventId, criterionUpdateStrategy, courseId);
    }

    private void updateCriteriaRewards(List<CriterionRewardDetailsRequestDto> request, Long criterionId, Long courseId) {
        criterionRewardRepository.deleteByCriterionId(criterionId);

        List<CriterionReward> criteriaRewards = request.stream().map(criterionReward -> {
            Reward reward = rewardRepository.findByKeyAndCourseId(criterionReward.getRewardKey(), courseId);

            return CriterionReward.builder().id(new CriterionReward.Id(criterionId, reward.getId())).reward(reward).maxAmount(criterionReward.getMaxAmount()).criterion(criterionRepository.getReferenceById(criterionId)).build();
        }).collect(Collectors.toList());

        criterionRewardRepository.saveAll(criteriaRewards);
    }

    private void updateAllCriteriaRewards(List<EventSectionDetailsRequestDto> eventSections, Long courseId) {
        eventSections.forEach(eventSection ->
                eventSection.getGradableEvents().forEach(gradableEvent ->
                        gradableEvent.getCriteria().forEach(criterion ->
                                updateCriteriaRewards(
                                        criterion.getRewards(),
                                        criterionRepository.findIdByKeyAndCourseId(criterion.getKey(), courseId),
                                        courseId
                                )
                        )
                )
        );
    }

    private void updateEventSections(List<EventSectionDetailsRequestDto> request, List<EventSectionDetailsRequestDto> currentConfig, Long courseId) {
        updateEntities(request, currentConfig, courseId, eventSectionUpdateStrategy, courseId);
        Map<String, EventSectionDetailsRequestDto> currentConfigByKey = currentConfig.stream().collect(Collectors.toMap(EventSectionDetailsRequestDto::getKey, Function.identity()));

        request.forEach(eventSection -> {
            EventSectionDetailsRequestDto currentSection = currentConfigByKey.get(eventSection.getKey());

            updateGradableEvents(
                    new ArrayList<>(eventSection.getGradableEvents()),
                    currentSection != null
                            ? new ArrayList<>(currentSection.getGradableEvents())
                            : List.of(), eventSectionRepository.findIdByKeyAndCourseId(eventSection.getKey(), courseId),
                    courseId
            );
        });
    }

    private void updateChests(List<ChestDetailsRequestDto> request, List<ChestDetailsRequestDto> currentConfig, Long courseId) {
        updateEntities(request, currentConfig, courseId, chestUpdateStrategy, courseId);
    }

    private void updateItems(List<ItemDetailsRequestDto> request, List<ItemDetailsRequestDto> currentConfig, Long courseId) {
        updateEntities(request, currentConfig, courseId, itemUpdateStrategy, courseId);
    }

    private void updateEvolutionStages(List<EvolutionStageDetailsRequestDto> request, List<EvolutionStageDetailsRequestDto> currentConfig, Long courseId) {
        updateEntities(request, currentConfig, courseId, evolutionStageUpdateStrategy, courseId);
    }

    public <Dto, Entity> void updateEntities(List<Dto> request, List<Dto> currentConfig, Long parentEntityId, EntityUpdateStrategy<Dto, Entity> strategy, Long courseId) {
        if (request.equals(currentConfig)) return;

        Map<Dto, Long> orderIds = CourseImportUtil.enumerateAsMap(request);

        List<Dto> newEntities = CourseImportUtil.getNewEntities(request, currentConfig, strategy.getKeyExtractor());
        List<Dto> deletedEntities = CourseImportUtil.getDeletedEntities(request, currentConfig, strategy.getKeyExtractor());
        List<Dto> modifiedEntities = CourseImportUtil.getModifiedEntities(request, currentConfig, strategy.getKeyExtractor());

        if (strategy.getTypeExtractor() != null) {
            CourseImportUtil.handleTypeChangedObjects(newEntities, deletedEntities, modifiedEntities, currentConfig, strategy.getKeyExtractor(), strategy.getTypeExtractor());
        }

        deleteEntities(deletedEntities, strategy, courseId);
        createEntities(newEntities, orderIds, parentEntityId, strategy);
        updateExistingEntities(modifiedEntities, orderIds, parentEntityId, strategy, courseId);
    }

    private <Dto, Entity> void deleteEntities(List<Dto> entities, EntityUpdateStrategy<Dto, Entity> strategy, Long courseId) {
        if (entities.isEmpty()) return;

        List<String> keys = entities.stream().map(strategy.getKeyExtractor()).toList();

        List<Entity> entitiesToDelete = strategy.findAllByKeys(keys, courseId);
        strategy.getRepository().deleteAll(entitiesToDelete);
        strategy.flush();
    }

    private <Dto, Entity> void createEntities(List<Dto> items, Map<Dto, Long> orderIds, Long parentEntityId, EntityUpdateStrategy<Dto, Entity> strategy) {
        if (items.isEmpty()) return;

        List<Entity> newEntities = items.stream().map(dto -> {
            Entity entity = strategy.createNewEntity(dto);
            return strategy.updateEntity(entity, dto, orderIds, parentEntityId);
        }).toList();

        strategy.getRepository().saveAll(newEntities);
        strategy.flush();
    }

    private <Dto, Entity> void updateExistingEntities(List<Dto> entities, Map<Dto, Long> orderIds, Long parentEntityId, EntityUpdateStrategy<Dto, Entity> strategy, Long courseId) {
        if (entities.isEmpty()) return;

        List<String> keys = entities.stream()
                .map(strategy.getKeyExtractor())
                .toList();

        Map<String, Entity> entitiesByKey = strategy.findAllByKeys(keys, courseId).stream()
                .collect(
                        Collectors.toMap(
                                strategy.getEntityKeyExtractor(),
                                Function.identity()
                        )
                );

        List<Entity> updatedEntities = entities.stream()
                .map(dto -> {
                    String key = strategy.getKeyExtractor().apply(dto);
                    Entity entity = entitiesByKey.get(key);
                    return strategy.updateEntity(entity, dto, orderIds, parentEntityId);
                })
                .collect(Collectors.toCollection(ArrayList::new));

        strategy.getRepository().saveAll(updatedEntities);
        strategy.flush();
    }

}
