package com.agh.polymorphia_backend.service.mapper;

import com.agh.polymorphia_backend.dto.request.course_import.EvolutionStageDetailsRequestDto;
import com.agh.polymorphia_backend.dto.request.course_import.SubmissionRequirementDetailsRequestDto;
import com.agh.polymorphia_backend.dto.request.course_import.criterion.CriterionDetailsRequestDto;
import com.agh.polymorphia_backend.dto.request.course_import.criterion.CriterionRewardDetailsRequestDto;
import com.agh.polymorphia_backend.dto.request.course_import.event_section.AssignmentSectionDetailsRequestDto;
import com.agh.polymorphia_backend.dto.request.course_import.event_section.EventSectionDetailsRequestDto;
import com.agh.polymorphia_backend.dto.request.course_import.event_section.ProjectSectionDetailsRequestDto;
import com.agh.polymorphia_backend.dto.request.course_import.event_section.TaskSectionDetailsRequestDto;
import com.agh.polymorphia_backend.dto.request.course_import.event_section.TestSectionDetailsRequestDto;
import com.agh.polymorphia_backend.dto.request.course_import.gradable_event.AssignmentDetailsRequestDto;
import com.agh.polymorphia_backend.dto.request.course_import.gradable_event.GradableEventDetailsRequestDto;
import com.agh.polymorphia_backend.dto.request.course_import.gradable_event.ProjectDetailsRequestDto;
import com.agh.polymorphia_backend.dto.request.course_import.gradable_event.TaskDetailsRequestDto;
import com.agh.polymorphia_backend.dto.request.course_import.gradable_event.TestDetailsRequestDto;
import com.agh.polymorphia_backend.dto.request.course_import.reward.ChestDetailsRequestDto;
import com.agh.polymorphia_backend.dto.request.course_import.reward.FlatBonusItemDetailsRequestDto;
import com.agh.polymorphia_backend.dto.request.course_import.reward.ItemDetailsRequestDto;
import com.agh.polymorphia_backend.dto.request.course_import.reward.PercentageBonusItemDetailsRequestDto;
import com.agh.polymorphia_backend.dto.request.course_import.variant.VariantCategoryDetailsRequestDto;
import com.agh.polymorphia_backend.dto.request.course_import.variant.VariantDetailsRequestDto;
import com.agh.polymorphia_backend.model.user.student.EvolutionStage;
import com.agh.polymorphia_backend.repository.criterion.projection.CriterionDetailsProjection;
import com.agh.polymorphia_backend.repository.criterion.projection.CriterionRewardDetailsProjection;
import com.agh.polymorphia_backend.repository.event_section.projection.EventSectionDetailsProjection;
import com.agh.polymorphia_backend.repository.gradable_event.projections.GradableEventDetailsProjection;
import com.agh.polymorphia_backend.repository.gradable_event.projections.ProjectDetailsDetailsProjection;
import com.agh.polymorphia_backend.repository.project.projection.ProjectVariantCategoryDetailsProjection;
import com.agh.polymorphia_backend.repository.project.projection.ProjectVariantDetailsProjection;
import com.agh.polymorphia_backend.repository.reward.projection.ChestDetailsDetailsProjection;
import com.agh.polymorphia_backend.repository.reward.projection.ItemDetailsDetailsProjection;
import com.agh.polymorphia_backend.repository.submission.projection.SubmissionRequirementDetailsProjection;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class CourseDetailsMapper {

    public List<EventSectionDetailsRequestDto> toEventSectionsDetailsRequestDto(
            List<EventSectionDetailsProjection> eventSections,
            Map<Long, List<CriterionDetailsProjection>> criteriaByGradableEvent,
            Map<Long, List<CriterionRewardDetailsProjection>> criteriaRewardsByCriterion,
            Map<Long, List<SubmissionRequirementDetailsProjection>> submissionRequirementsByGradableEvent,
            Map<Long, List<GradableEventDetailsProjection>> gradableEvents,
            Map<Long, List<ProjectDetailsDetailsProjection>> projectDetails,
            Map<Long, List<ProjectVariantCategoryDetailsProjection>> projectVariantCategoriesByProject,
            Map<Long, List<ProjectVariantDetailsProjection>> projectVariantsByCategory

    ) {
        return eventSections.stream()
                .map(eventSection -> toEventSectionDetailsRequestDto(
                        eventSection,
                        gradableEvents.getOrDefault(eventSection.getId(), List.of()),
                        projectDetails,
                        criteriaByGradableEvent,
                        criteriaRewardsByCriterion,
                        submissionRequirementsByGradableEvent,
                        projectVariantCategoriesByProject,
                        projectVariantsByCategory
                ))
                .toList();
    }

    public List<EvolutionStageDetailsRequestDto> toEvolutionStagesDetailsRequestDto(List<EvolutionStage> evolutionStages) {
        return evolutionStages.stream()
                .sorted(Comparator.comparing(EvolutionStage::getOrderIndex))
                .map(this::toEvolutionStageDetailsRequestDto)
                .toList();
    }

    private EventSectionDetailsRequestDto toEventSectionDetailsRequestDto(
            EventSectionDetailsProjection eventSection,
            List<GradableEventDetailsProjection> gradableEvents,
            Map<Long, List<ProjectDetailsDetailsProjection>> projectDetailsMap,
            Map<Long, List<CriterionDetailsProjection>> criteriaByEvent,
            Map<Long, List<CriterionRewardDetailsProjection>> rewardsByCriterion,
            Map<Long, List<SubmissionRequirementDetailsProjection>> requirementsByEvent,
            Map<Long, List<ProjectVariantCategoryDetailsProjection>> projectVariantCategoriesByProject,
            Map<Long, List<ProjectVariantDetailsProjection>> projectVariantsByCategory) {

        return switch (eventSection.getEventSectionType()) {
            case ASSIGNMENT -> toAssignmentSection(
                    eventSection,
                    gradableEvents,
                    criteriaByEvent,
                    rewardsByCriterion,
                    requirementsByEvent
            );
            case TEST -> toTestSection(
                    eventSection,
                    gradableEvents,
                    criteriaByEvent,
                    rewardsByCriterion
            );
            case PROJECT -> toProjectSection(
                    eventSection,
                    gradableEvents,
                    projectDetailsMap,
                    criteriaByEvent,
                    rewardsByCriterion,
                    projectVariantCategoriesByProject,
                    projectVariantsByCategory
            );
            case TASK -> toTaskSection(
                    eventSection,
                    gradableEvents,
                    criteriaByEvent,
                    rewardsByCriterion
            );
        };
    }

    private EventSectionDetailsRequestDto toAssignmentSection(
            EventSectionDetailsProjection eventSection,
            List<GradableEventDetailsProjection> gradableEvents,
            Map<Long, List<CriterionDetailsProjection>> criteriaByEvent,
            Map<Long, List<CriterionRewardDetailsProjection>> rewardsByCriterion,
            Map<Long, List<SubmissionRequirementDetailsProjection>> submissionRequirementsByEvent) {

        List<AssignmentDetailsRequestDto> assignments = gradableEvents.stream()
                .map(gradableEvent -> toAssignmentDto(
                        gradableEvent,
                        submissionRequirementsByEvent,
                        criteriaByEvent.getOrDefault(gradableEvent.getId(), Collections.emptyList()),
                        rewardsByCriterion
                ))
                .toList();

        AssignmentSectionDetailsRequestDto.AssignmentSectionDetailsRequestDtoBuilder<?, ?> response = AssignmentSectionDetailsRequestDto.builder()
                .gradableEvents(assignments);

        return applyCommonEventSectionFields(response, eventSection);
    }

    private EventSectionDetailsRequestDto toTestSection(
            EventSectionDetailsProjection section,
            List<GradableEventDetailsProjection> gradableEvents,
            Map<Long, List<CriterionDetailsProjection>> criteriaByGradableEvent,
            Map<Long, List<CriterionRewardDetailsProjection>> rewardsByCriterion) {


        List<TestDetailsRequestDto> tests = gradableEvents.stream()
                .map(test -> toTestDto(test, criteriaByGradableEvent.getOrDefault(test.getId(), Collections.emptyList()), rewardsByCriterion))
                .toList();

        TestSectionDetailsRequestDto.TestSectionDetailsRequestDtoBuilder<?, ?> response = TestSectionDetailsRequestDto.builder()
                .gradableEvents(tests);

        return applyCommonEventSectionFields(response, section);
    }

    private EventSectionDetailsRequestDto toTaskSection(
            EventSectionDetailsProjection section,
            List<GradableEventDetailsProjection> gradableEvents,
            Map<Long, List<CriterionDetailsProjection>> criteriaByGradableEvent,
            Map<Long, List<CriterionRewardDetailsProjection>> rewardsByCriterion) {

        List<TaskDetailsRequestDto> tasks = gradableEvents.stream()
                .map(task -> toTaskDto(task, criteriaByGradableEvent.getOrDefault(task.getId(), Collections.emptyList()), rewardsByCriterion))
                .toList();

        TaskSectionDetailsRequestDto.TaskSectionDetailsRequestDtoBuilder<?, ?> response = TaskSectionDetailsRequestDto.builder()
                .gradableEvents(tasks);

        return applyCommonEventSectionFields(response, section);
    }

    private EventSectionDetailsRequestDto toProjectSection(
            EventSectionDetailsProjection section,
            List<GradableEventDetailsProjection> gradableEvents,
            Map<Long, List<ProjectDetailsDetailsProjection>> projectDetailsByGradableEvent,
            Map<Long, List<CriterionDetailsProjection>> criteriaByGradableEvent,
            Map<Long, List<CriterionRewardDetailsProjection>> rewardsByCriterion,
            Map<Long, List<ProjectVariantCategoryDetailsProjection>> projectVariantCategoriesByProject,
            Map<Long, List<ProjectVariantDetailsProjection>> projectVariantsByCategory) {

        List<ProjectDetailsRequestDto> projects = gradableEvents.stream()
                .map(project ->
                        toProjectDto(
                                project,
                                projectDetailsByGradableEvent.get(project.getId())
                                        .getFirst()
                                        .getAllowCrossCourseGroupProjectGroups(),
                                criteriaByGradableEvent.getOrDefault(project.getId(), Collections.emptyList()),
                                rewardsByCriterion,
                                projectVariantCategoriesByProject.getOrDefault(project.getId(), Collections.emptyList()),
                                projectVariantsByCategory
                        )
                )
                .toList();
        ProjectSectionDetailsRequestDto.ProjectSectionDetailsRequestDtoBuilder<?, ?> response = ProjectSectionDetailsRequestDto.builder()
                .gradableEvents(projects);

        return applyCommonEventSectionFields(response, section);
    }

    private List<VariantCategoryDetailsRequestDto> getVariantCategories(List<ProjectVariantCategoryDetailsProjection> projectVariantCategories,
                                                                        Map<Long, List<ProjectVariantDetailsProjection>> projectVariantsByCategory) {
        return projectVariantCategories.stream()
                .map(category -> getVariantCategory(category,
                        projectVariantsByCategory.getOrDefault(category.getId(), Collections.emptyList())))
                .toList();

    }

    private VariantCategoryDetailsRequestDto getVariantCategory(ProjectVariantCategoryDetailsProjection projectVariantCategory,
                                                                List<ProjectVariantDetailsProjection> variants) {
        return VariantCategoryDetailsRequestDto.builder()
                .key(projectVariantCategory.getKey())
                .name(projectVariantCategory.getName())
                .variants(variants.stream()
                        .map(variant -> VariantDetailsRequestDto.builder()
                                .key(variant.getKey())
                                .name(variant.getName())
                                .shortCode(variant.getShortCode())
                                .description(variant.getDescription())
                                .imageUrl(variant.getImageUrl())
                                .build())
                        .toList())
                .build();
    }

    private EventSectionDetailsRequestDto applyCommonEventSectionFields(
            EventSectionDetailsRequestDto.EventSectionDetailsRequestDtoBuilder<?, ?> builder,
            EventSectionDetailsProjection section
    ) {

        return builder
                .key(section.getKey())
                .name(section.getName())
                .isShownInRoadmap(section.getIsShownInRoadMap())
                .eventsWithTopics(section.getHasGradableEventsWithTopics())
                .isHidden(section.getIsHidden())
                .type(section.getEventSectionType())
                .build();
    }

    private AssignmentDetailsRequestDto toAssignmentDto(
            GradableEventDetailsProjection gradableEvent,
            Map<Long, List<SubmissionRequirementDetailsProjection>> submissionRequirementsByEvent,
            List<CriterionDetailsProjection> criteria,
            Map<Long, List<CriterionRewardDetailsProjection>> rewardsByCriterion
    ) {
        List<SubmissionRequirementDetailsRequestDto> requirements = submissionRequirementsByEvent
                .getOrDefault(gradableEvent.getId(), List.of())
                .stream()
                .map(this::toSubmissionRequirementDto)
                .toList();

        return (AssignmentDetailsRequestDto) applyCommonGradableEventFields(
                AssignmentDetailsRequestDto.builder()
                        .submissionRequirements(requirements),
                gradableEvent,
                criteria,
                rewardsByCriterion
        );
    }

    private TestDetailsRequestDto toTestDto(
            GradableEventDetailsProjection gradableEvent,
            List<CriterionDetailsProjection> criteria,
            Map<Long, List<CriterionRewardDetailsProjection>> rewardsByCriterion
    ) {
        return (TestDetailsRequestDto) applyCommonGradableEventFields(
                TestDetailsRequestDto.builder(),
                gradableEvent,
                criteria,
                rewardsByCriterion
        );
    }

    private TaskDetailsRequestDto toTaskDto(
            GradableEventDetailsProjection gradableEvent,
            List<CriterionDetailsProjection> criteria,
            Map<Long, List<CriterionRewardDetailsProjection>> rewardsByCriterion
    ) {
        return (TaskDetailsRequestDto) applyCommonGradableEventFields(
                TaskDetailsRequestDto.builder(),
                gradableEvent,
                criteria,
                rewardsByCriterion
        );
    }

    private ProjectDetailsRequestDto toProjectDto(
            GradableEventDetailsProjection gradableEvent,
            Boolean allowCrossCourseGroupProjectGroup,
            List<CriterionDetailsProjection> criteria,
            Map<Long, List<CriterionRewardDetailsProjection>> rewardsByCriterion,
            List<ProjectVariantCategoryDetailsProjection> projectVariantCategories,
            Map<Long, List<ProjectVariantDetailsProjection>> projectVariantsByCategory
    ) {
        return (ProjectDetailsRequestDto) applyCommonGradableEventFields(
                ProjectDetailsRequestDto
                        .builder()
                        .variantCategories(getVariantCategories(projectVariantCategories, projectVariantsByCategory))
                        .allowCrossCourseGroupProjectGroup(allowCrossCourseGroupProjectGroup),
                gradableEvent,
                criteria,
                rewardsByCriterion
        );
    }

    private GradableEventDetailsRequestDto applyCommonGradableEventFields(
            GradableEventDetailsRequestDto.GradableEventDetailsRequestDtoBuilder<?, ?> builder,
            GradableEventDetailsProjection gradableEvent,
            List<CriterionDetailsProjection> criteria,
            Map<Long, List<CriterionRewardDetailsProjection>> rewardsByCriterion) {

        List<CriterionDetailsRequestDto> criteriaDto = criteria.stream()
                .map(criterion -> toCriterionDto(
                        criterion,
                        rewardsByCriterion.getOrDefault(criterion.getId(), Collections.emptyList()))
                )
                .toList();

        return builder
                .key(gradableEvent.getKey())
                .name(gradableEvent.getName())
                .topic(gradableEvent.getTopic())
                .markdownSourceUrl(gradableEvent.getMarkdownSourceUrl())
                .isHidden(gradableEvent.getIsHidden())
                .isLocked(gradableEvent.getIsLocked())
                .type(gradableEvent.getType())
                .criteria(criteriaDto)
                .build();
    }


    private SubmissionRequirementDetailsRequestDto toSubmissionRequirementDto(
            SubmissionRequirementDetailsProjection submissionRequirement) {
        return SubmissionRequirementDetailsRequestDto.builder()
                .name(submissionRequirement.getName())
                .isMandatory(submissionRequirement.getIsMandatory())
                .key(submissionRequirement.getKey())
                .build();
    }

    private CriterionDetailsRequestDto toCriterionDto(
            CriterionDetailsProjection criterion,
            List<CriterionRewardDetailsProjection> rewards) {

        List<CriterionRewardDetailsRequestDto> criterionRewards = rewards.stream()
                .map(r -> CriterionRewardDetailsRequestDto.builder()
                        .rewardKey(r.getRewardKey())
                        .maxAmount(r.getMaxAmount())
                        .build()
                )
                .toList();

        return CriterionDetailsRequestDto.builder()
                .name(criterion.getName())
                .maxXp(criterion.getMaxXp())
                .key(criterion.getKey())
                .rewards(criterionRewards)
                .build();
    }

    private EvolutionStageDetailsRequestDto toEvolutionStageDetailsRequestDto(EvolutionStage evolutionStage) {
        return EvolutionStageDetailsRequestDto.builder()
                .key(evolutionStage.getKey())
                .name(evolutionStage.getName())
                .minXp(evolutionStage.getMinXp())
                .description(evolutionStage.getDescription())
                .imageUrl(evolutionStage.getImageUrl())
                .grade(evolutionStage.getGrade())
                .build();
    }

    public List<ItemDetailsRequestDto> toItemsDetailsRequestDto(List<ItemDetailsDetailsProjection> items) {
        return items.stream()
                .map(this::toItemDetailsRequestDto)
                .toList();
    }

    public List<ChestDetailsRequestDto> toChestsDetailsRequestDto(List<ChestDetailsDetailsProjection> chests) {
        return chests.stream()
                .map(this::toChestDetailsRequestDto)
                .toList();
    }

    private ChestDetailsRequestDto toChestDetailsRequestDto(ChestDetailsDetailsProjection chest) {
        return ChestDetailsRequestDto.builder()
                .key(chest.getKey())
                .name(chest.getName())
                .description(chest.getDescription())
                .behavior(chest.getBehavior())
                .itemKeys(chest.getItemKeys())
                .imageUrl(chest.getImageUrl())
                .build();
    }

    private ItemDetailsRequestDto toItemDetailsRequestDto(ItemDetailsDetailsProjection item) {
        return switch (item.getItemType()) {
            case FLAT_BONUS -> applyCommonItemFields(
                    FlatBonusItemDetailsRequestDto.builder()
                            .xpBonus(item.getXpBonus())
                            .behavior(item.getBehavior()),
                    item
            );
            case PERCENTAGE_BONUS -> applyCommonItemFields(
                    PercentageBonusItemDetailsRequestDto.builder()
                            .percentageBonus(item.getPercentageBonus()),
                    item
            );
        };
    }

    private <T extends ItemDetailsRequestDto.ItemDetailsRequestDtoBuilder<?, ?>> ItemDetailsRequestDto applyCommonItemFields(
            T builder, ItemDetailsDetailsProjection item) {
        return builder
                .name(item.getName())
                .key(item.getKey())
                .description(item.getDescription())
                .imageUrl(item.getImageUrl())
                .limit(item.getLimit())
                .type(item.getItemType())
                .eventSectionKey(item.getEventSectionKey())
                .build();
    }
}
