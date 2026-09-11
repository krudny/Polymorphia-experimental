package com.agh.polymorphia_backend.service.gradable_event;

import com.agh.polymorphia_backend.dto.response.reward.points_summary.PointsSummaryResponseDto;
import com.agh.polymorphia_backend.model.event_section.EventSection;
import com.agh.polymorphia_backend.model.hall_of_fame.StudentScoreDetail;
import com.agh.polymorphia_backend.model.reward.assigned.AssignedItem;
import com.agh.polymorphia_backend.model.reward.item.ItemType;
import com.agh.polymorphia_backend.model.user.student.Animal;
import com.agh.polymorphia_backend.service.event_section.EventSectionService;
import com.agh.polymorphia_backend.service.hall_of_fame.HallOfFameService;
import com.agh.polymorphia_backend.service.mapper.PointsSummaryMapper;
import com.agh.polymorphia_backend.service.reward.AssignedRewardService;
import com.agh.polymorphia_backend.service.student.AnimalService;
import com.agh.polymorphia_backend.service.user.UserService;
import com.agh.polymorphia_backend.service.validation.AccessAuthorizer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class PointsSummaryService {

    private final AccessAuthorizer accessAuthorizer;
    private final EventSectionService eventSectionService;
    private final HallOfFameService hallOfFameService;
    private final AnimalService animalService;
    private final UserService userService;
    private final AssignedRewardService assignedRewardService;
    private final PointsSummaryMapper pointsSummaryMapper;

    public PointsSummaryResponseDto getPointsSummary(Long eventSectionId) {
        EventSection eventSection = eventSectionService.getEventSection(eventSectionId);

        Long courseId = eventSection.getCourse().getId();
        accessAuthorizer.authorizeCurrentUserCourseAccess(courseId);

        Long userId = userService.getCurrentUser().getUserId();
        Animal animal = animalService.getAnimal(userId, courseId);

        StudentScoreDetail scoreDetail = hallOfFameService.getStudentEventSectionScoreDetails(animal.getId(), eventSectionId);
        List<AssignedItem> assignedItems = assignedRewardService.getAnimalEventSectionAssignedItems(animal.getId(), eventSectionId);
        List<AssignedItem> percentageBonusItems = assignedRewardService.filterAssignedItemsByType(assignedItems, ItemType.PERCENTAGE_BONUS);
        List<AssignedItem> flatBonusItems = assignedRewardService.filterAssignedItemsByType(assignedItems, ItemType.FLAT_BONUS);

        return pointsSummaryMapper.toPointsSummaryResponseDto(scoreDetail, flatBonusItems, percentageBonusItems);
    }
}
