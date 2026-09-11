package com.agh.polymorphia_backend.service.course;

import com.agh.polymorphia_backend.dto.request.course_import.CourseDetailsRequestDto;
import com.agh.polymorphia_backend.dto.request.course_import.event_section.EventSectionDetailsRequestDto;
import com.agh.polymorphia_backend.model.course.Course;
import com.agh.polymorphia_backend.model.user.AbstractRoleUser;
import com.agh.polymorphia_backend.model.user.student.EvolutionStage;
import com.agh.polymorphia_backend.repository.course.EvolutionStagesRepository;
import com.agh.polymorphia_backend.repository.criterion.CriterionRepository;
import com.agh.polymorphia_backend.repository.criterion.projection.CriterionDetailsProjection;
import com.agh.polymorphia_backend.repository.criterion.projection.CriterionRewardDetailsProjection;
import com.agh.polymorphia_backend.repository.event_section.EventSectionRepository;
import com.agh.polymorphia_backend.repository.gradable_event.GradableEventRepository;
import com.agh.polymorphia_backend.repository.gradable_event.projections.GradableEventDetailsProjection;
import com.agh.polymorphia_backend.repository.gradable_event.projections.ProjectDetailsDetailsProjection;
import com.agh.polymorphia_backend.repository.project.ProjectVariantCategoryRepository;
import com.agh.polymorphia_backend.repository.project.ProjectVariantRepository;
import com.agh.polymorphia_backend.repository.project.projection.ProjectVariantCategoryDetailsProjection;
import com.agh.polymorphia_backend.repository.project.projection.ProjectVariantDetailsProjection;
import com.agh.polymorphia_backend.repository.reward.ChestRepository;
import com.agh.polymorphia_backend.repository.reward.ItemRepository;
import com.agh.polymorphia_backend.repository.reward.projection.ChestDetailsDetailsProjection;
import com.agh.polymorphia_backend.repository.reward.projection.ItemDetailsDetailsProjection;
import com.agh.polymorphia_backend.repository.submission.SubmissionRequirementRepository;
import com.agh.polymorphia_backend.repository.submission.projection.SubmissionRequirementDetailsProjection;
import com.agh.polymorphia_backend.service.event_section.EventSectionService;
import com.agh.polymorphia_backend.service.mapper.CourseDetailsMapper;
import com.agh.polymorphia_backend.service.user.UserService;
import com.agh.polymorphia_backend.service.validation.AccessAuthorizer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CourseDetailsService {

    private final AccessAuthorizer accessAuthorizer;
    private final UserService userService;
    private final CourseDetailsMapper courseDetailsMapper;
    private final ItemRepository itemRepository;
    private final ChestRepository chestRepository;
    private final CourseService courseService;
    private final EvolutionStagesRepository evolutionStagesRepository;
    private final SubmissionRequirementRepository submissionRequirementRepository;
    private final CriterionRepository criterionRepository;
    private final EventSectionRepository eventSectionRepository;
    private final GradableEventRepository gradableEventRepository;
    private final ProjectVariantCategoryRepository projectVariantCategoryRepository;
    private final ProjectVariantRepository projectVariantRepository;
    private final EventSectionService eventSectionService;

    @Transactional(readOnly = true)
    public CourseDetailsRequestDto getCourseDetails(Long courseId) {
        Course course = courseService.getCourseById(courseId);
        AbstractRoleUser user = userService.getCurrentUser();
        accessAuthorizer.isCourseAccessAuthorized(user, courseId);
        List<ItemDetailsDetailsProjection> items = itemRepository.findAllItemDetailsByCourseId(courseId);
        List<ChestDetailsDetailsProjection> chests = chestRepository.findAllChestDetailsByCourseId(courseId);
        List<EvolutionStage> evolutionStages = evolutionStagesRepository.findAllByCourseId(courseId);
        List<GradableEventDetailsProjection> gradableEvents = gradableEventRepository.findGradableEventsByCourseId(courseId);
        Map<Long, List<GradableEventDetailsProjection>> gradableEventsByEventSection = groupBy(
                gradableEvents,
                GradableEventDetailsProjection::getEventSectionId
        );

        return CourseDetailsRequestDto.builder()
                .name(course.getName())
                .imageUrl(course.getImageUrl())
                .coordinatorImageUrl(course.getCoordinatorImageUrl())
                .instructorImageUrl(course.getInstructorImageUrl())
                .markdownSourceUrl(course.getMarkdownSourceUrl())
                .eventSections(getEventSectionDetails(courseId, gradableEventsByEventSection))
                .evolutionStages(courseDetailsMapper.toEvolutionStagesDetailsRequestDto(evolutionStages))
                .items(courseDetailsMapper.toItemsDetailsRequestDto(items))
                .chests(courseDetailsMapper.toChestsDetailsRequestDto(chests))
                .roadmapOrderKeys(getRoadmapOrder(gradableEvents))
                .build();
    }

    private List<String> getRoadmapOrder(List<GradableEventDetailsProjection> gradableEvents) {
        return gradableEvents.stream()
                .filter(gradableEvent-> eventSectionService.getEventSection(gradableEvent.getEventSectionId()).isShownInRoadMap())
                .sorted(Comparator.comparing(GradableEventDetailsProjection::getRoadmapOrderIndex))
                .map(GradableEventDetailsProjection::getKey)
                .toList();
    }

    private List<EventSectionDetailsRequestDto> getEventSectionDetails(
            Long courseId,
            Map<Long, List<GradableEventDetailsProjection>> gradableEvents
    ) {

        return courseDetailsMapper.toEventSectionsDetailsRequestDto(
                eventSectionRepository.findBasicByCourseId(courseId),
                getCriteriaByGradableEvent(courseId),
                getCriteriaRewardsByCriterion(courseId),
                getSubmissionRequirementsByGradableEvent(courseId),
                gradableEvents,
                getProjectDetailsByProject(courseId),
                getProjectVariantCategoryDetailsByProject(courseId),
                getProjectVariantDetailsByProject(courseId)
        );
    }

    private Map<Long, List<ProjectVariantCategoryDetailsProjection>> getProjectVariantCategoryDetailsByProject(Long courseId) {
        return groupBy(
                projectVariantCategoryRepository.findAllByCourseId(courseId),
                ProjectVariantCategoryDetailsProjection::getProjectId
        );
    }

    private Map<Long, List<ProjectVariantDetailsProjection>> getProjectVariantDetailsByProject(Long courseId) {
        return groupBy(
                projectVariantRepository.findAllByCourseId(courseId),
                ProjectVariantDetailsProjection::getVariantCategoryId
        );
    }

    private Map<Long, List<ProjectDetailsDetailsProjection>> getProjectDetailsByProject(Long courseId) {
        return groupBy(
                gradableEventRepository.findProjectDetailsByCourseId(courseId),
                ProjectDetailsDetailsProjection::getId
        );
    }


    private Map<Long, List<CriterionDetailsProjection>> getCriteriaByGradableEvent(Long courseId) {
        return groupBy(
                criterionRepository.findByCourseId(courseId),
                CriterionDetailsProjection::getGradableEventId
        );
    }

    private Map<Long, List<CriterionRewardDetailsProjection>> getCriteriaRewardsByCriterion(Long courseId) {
        return groupBy(
                criterionRepository.findCriteriaRewardsByCourseId(courseId),
                CriterionRewardDetailsProjection::getCriterionId
        );
    }

    private Map<Long, List<SubmissionRequirementDetailsProjection>> getSubmissionRequirementsByGradableEvent(Long courseId) {
        return groupBy(
                submissionRequirementRepository.findByCourseId(courseId),
                SubmissionRequirementDetailsProjection::getGradableEventId
        );
    }


    private <T, K> Map<K, List<T>> groupBy(List<T> list, Function<? super T, ? extends K> keyExtractor) {
        if (list == null) {
            return Map.of();
        }
        return list.stream()
                .collect(Collectors.groupingBy(keyExtractor));
    }
}
