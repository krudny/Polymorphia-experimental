package com.agh.polymorphia_backend.service.validation;

import com.agh.polymorphia_backend.dto.request.grade.CriterionGradeRequestDto;
import com.agh.polymorphia_backend.dto.request.grade.GradeRequestDto;
import com.agh.polymorphia_backend.dto.request.reward.ShortAssignedRewardRequestDto;
import com.agh.polymorphia_backend.dto.request.target.StudentGroupTargetRequestDto;
import com.agh.polymorphia_backend.dto.request.target.StudentTargetRequestDto;
import com.agh.polymorphia_backend.dto.request.target.TargetRequestDto;
import com.agh.polymorphia_backend.model.criterion.Criterion;
import com.agh.polymorphia_backend.model.criterion.CriterionReward;
import com.agh.polymorphia_backend.model.gradable_event.GradableEvent;
import com.agh.polymorphia_backend.model.reward.Item;
import com.agh.polymorphia_backend.model.reward.Reward;
import com.agh.polymorphia_backend.model.reward.RewardType;
import com.agh.polymorphia_backend.service.criterion.CriterionService;
import com.agh.polymorphia_backend.service.project.ProjectGroupService;
import com.agh.polymorphia_backend.service.reward.AssignedRewardService;
import com.agh.polymorphia_backend.service.reward.RewardService;
import com.agh.polymorphia_backend.service.user.UserService;
import com.agh.polymorphia_backend.util.NumberFormatter;
import lombok.AllArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@AllArgsConstructor
public class GradingValidator {
    private final CriterionService criterionService;
    private final RewardService rewardService;
    private final AssignedRewardService assignedRewardService;
    private final ProjectGroupService projectGroupService;
    private final UserService userService;

    public void validate(GradeRequestDto request, GradableEvent gradableEvent) {
        request.getCriteria().forEach((criterionId, criterionRequest) ->
                validateCriterion(request.getTarget(), gradableEvent, criterionId, criterionRequest));
    }

    private void validateCriterion(TargetRequestDto target, GradableEvent gradableEvent, Long criterionId, CriterionGradeRequestDto criterionRequest) {
        Criterion criterion = criterionService.findById(criterionId);
        validateCriterionInGradableEvent(criterion, gradableEvent);
        validateXpOverload(criterion, criterionRequest);
        validateAssignedRewards(target, criterion, criterionRequest);
    }


    private void validateCriterionInGradableEvent(Criterion criterion, GradableEvent gradableEvent) {
        if (!criterion.getGradableEvent().equals(gradableEvent)) {
            String message = String.format("Kryterium %d nie należy do wydarzenia %s.", criterion.getId(), gradableEvent.getName());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
    }

    private void validateXpOverload(Criterion criterion, CriterionGradeRequestDto criterionGradeRequest) {
        if (criterion.getMaxXp().compareTo(criterionGradeRequest.getGainedXp()) < 0) {
            String message = String.format(
                    "Do tego kryterium można przypisać maksymalnie %s xp.",
                    NumberFormatter.formatToString(criterion.getMaxXp())
            );

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
    }

    private void validateAssignedRewards(TargetRequestDto target, Criterion criterion, CriterionGradeRequestDto criterionGradeRequest) {
        criterionGradeRequest.getAssignedRewards().forEach(reward -> validateAssignedReward(target, criterion, reward));
    }

    private void validateAssignedReward(TargetRequestDto target, Criterion criterion, ShortAssignedRewardRequestDto rewardRequest) {
        Reward reward = rewardService.findById(rewardRequest.getRewardId());
        Integer quantity = rewardRequest.getQuantity();
        Integer maxAmount = criterion.getAssignableRewards().stream()
                .filter(assignableReward -> assignableReward.getReward().getId().equals(rewardRequest.getRewardId()))
                .findFirst()
                .map(CriterionReward::getMaxAmount)
                .orElse(0);


        if (maxAmount < quantity) {
            String message = String.format("Do tego kryterium można przypisać maksymalnie %s nagród typu %s.", maxAmount, reward.getName());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }

        switch (target.type()) {
            case STUDENT ->
                    validateStudentLimitAssignedRewards(((StudentTargetRequestDto) target).id(), reward, quantity, criterion.getId());
            case STUDENT_GROUP ->
                    validateStudentGroupLimitAssignedRewards(((StudentGroupTargetRequestDto) target).groupId(), reward, quantity, criterion.getId());
        }
    }

    private void validateStudentGroupLimitAssignedRewards(Long groupId, Reward reward, Integer quantity, Long criterionId) {
        List<Long> studentIds = projectGroupService.getStudentIdsFromProjectGroup(groupId);
        studentIds.forEach(studentId -> validateStudentLimitAssignedRewards(studentId, reward, quantity, criterionId));
    }

    private void validateStudentLimitAssignedRewards(Long studentId, Reward reward, Integer quantity, Long criterionId) {
        if (reward.getRewardType().equals(RewardType.ITEM)
                && assignedRewardService.willLimitBeCrossedWithNewItems((Item) Hibernate.unproxy(reward), studentId, quantity, criterionId)) {
            String userName = userService.getFullName(userService.findById(studentId));
            String message = String.format("Limit nagród typu %s został osiągnięty dla użytkownika %s.", reward.getName(), userName);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
    }


}
