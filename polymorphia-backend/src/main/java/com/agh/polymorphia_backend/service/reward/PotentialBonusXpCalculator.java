package com.agh.polymorphia_backend.service.reward;

import com.agh.polymorphia_backend.dto.response.equipment.potential_xp.ChestPotentialXpResponseDto;
import com.agh.polymorphia_backend.dto.response.equipment.potential_xp.ChestPotentialXpResponseDtoWithType;
import com.agh.polymorphia_backend.dto.response.equipment.potential_xp.PotentialBonusXpResponseDto;
import com.agh.polymorphia_backend.model.grade.Grade;
import com.agh.polymorphia_backend.model.reward.Chest;
import com.agh.polymorphia_backend.model.reward.FlatBonusItem;
import com.agh.polymorphia_backend.model.reward.Item;
import com.agh.polymorphia_backend.model.reward.PercentageBonusItem;
import com.agh.polymorphia_backend.model.reward.assigned.AssignedItem;
import com.agh.polymorphia_backend.model.reward.chest.ChestBehavior;
import com.agh.polymorphia_backend.model.reward.item.FlatBonusItemBehavior;
import com.agh.polymorphia_backend.model.reward.item.ItemType;
import com.agh.polymorphia_backend.service.grade.GradeService;
import com.agh.polymorphia_backend.service.hall_of_fame.HallOfFameService;
import com.agh.polymorphia_backend.service.mapper.PotentialXpMapper;
import lombok.AllArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;


@Service
@AllArgsConstructor
public class PotentialBonusXpCalculator {

    private final HallOfFameService hallOfFameService;
    private final GradeService gradeService;
    private final AssignedRewardService assignedRewardService;
    private final BonusXpCalculator bonusXpCalculator;
    private final RewardService rewardService;
    private final PotentialXpMapper potentialXpMapper;

    public ChestPotentialXpResponseDtoWithType getPotentialBonusXpForALLChest(Long animalId, Chest chest) {
        List<Item> items = chest.getItems();
        List<AssignedItem> currentAssignedItems = assignedRewardService.getAnimalAssignedItemsByType(
                animalId,
                ItemType.FLAT_BONUS
        );

        ChestPotentialXpResponseDto flatBonus = getPotentialFlatBonusXp(items, animalId, false, currentAssignedItems);

        Map<Long, BigDecimal> flatBonusByEventSection = new HashMap<>(
                assignedRewardService.getTotalBonusByEventSection(currentAssignedItems)
        );
        ChestPotentialXpResponseDto percentageBonus = getPotentialPercentageBonusXp(items, animalId, false, flatBonusByEventSection);

        PotentialBonusXpResponseDto combinedSummary = potentialXpMapper.combineSummaries(
                flatBonus.getSummary(),
                percentageBonus.getSummary()
        );

        Map<String, PotentialBonusXpResponseDto> combinedDetails = new HashMap<>();
        combinedDetails.putAll(flatBonus.getItemDetails());
        combinedDetails.putAll(percentageBonus.getItemDetails());

        ChestPotentialXpResponseDto chestResponse = ChestPotentialXpResponseDto.builder()
                .summary(combinedSummary)
                .itemDetails(combinedDetails)
                .build();

        return ChestPotentialXpResponseDtoWithType.builder()
                .behavior(ChestBehavior.ALL)
                .potentialXp(chestResponse)
                .build();
    }

    public ChestPotentialXpResponseDtoWithType getPotentialBonusXpForONEChest(Long studentId, Chest chest) {
        Map<String, PotentialBonusXpResponseDto> itemBonusXp = new HashMap<>();
        Map<Long, Long> countById = chest.getItems().stream()
                .collect(Collectors.groupingBy(Item::getId, Collectors.counting()));

        for (Item item : chest.getItems()) {
            List<PotentialBonusXpResponseDto> itemDetails = calculateSingleItemBonus(
                    studentId,
                    item
            ).values().stream().toList();

            Long currentCount = countById.get(item.getId());
            String newKey = item.getId() + "_" + (currentCount - 1);
            countById.put(item.getId(), currentCount - 1);
            itemBonusXp.put(newKey, itemDetails.getFirst());
        }

        ChestPotentialXpResponseDto chestResponse = ChestPotentialXpResponseDto.builder()
                .itemDetails(itemBonusXp)
                .build();

        return ChestPotentialXpResponseDtoWithType.builder()
                .behavior(ChestBehavior.ONE_OF_MANY)
                .potentialXp(chestResponse)
                .build();
    }


    private ChestPotentialXpResponseDto getPotentialFlatBonusXp(
            List<Item> items,
            Long animalId,
            boolean shouldAddLoss,
            List<AssignedItem> currentAssignedItems
    ) {
        List<FlatBonusItem> flatBonusItems = items.stream()
                .filter(item -> item.getItemType().equals(ItemType.FLAT_BONUS))
                .map(item -> (FlatBonusItem) item)
                .toList();

        if (flatBonusItems.isEmpty()) {
            return potentialXpMapper.toEmptyChestPotentialXpResponseDto();
        }

        return calculatePotentialFlatBonusXp(animalId, flatBonusItems, shouldAddLoss, currentAssignedItems);
    }

    private ChestPotentialXpResponseDto getPotentialPercentageBonusXp(List<Item> items, Long animalId,
                                                                      boolean shouldAddLoss,
                                                                      Map<Long, BigDecimal> flatBonusByEventSection) {
        List<Item> percentageBonusItems = rewardService.filterItemsByType(items, ItemType.PERCENTAGE_BONUS);
        List<AssignedItem> newAssignedItems = itemsToAssignedItems(percentageBonusItems);

        Optional<BigDecimal> lossXp = shouldAddLoss ? Optional.of(BigDecimal.valueOf(0.0)) : Optional.empty();

        newAssignedItems.forEach(assignedItem -> calculatePotentialPercentageBonusXp(animalId, flatBonusByEventSection, assignedItem));
        Map<String, PotentialBonusXpResponseDto> bonusByItem = potentialXpMapper.assignedItemsToItemDetails(newAssignedItems, lossXp);

        BigDecimal totalBonus = newAssignedItems.stream()
                .map(AssignedItem::getBonusXp)
                .reduce(BigDecimal.valueOf(0.0), BigDecimal::add);

        PotentialBonusXpResponseDto summary = potentialXpMapper.toChestPotentialXpResponseDto(totalBonus, lossXp);

        return ChestPotentialXpResponseDto.builder()
                .summary(summary)
                .itemDetails(bonusByItem)
                .build();
    }

    private ChestPotentialXpResponseDto calculatePotentialFlatBonusXp(
            Long animalId,
            List<FlatBonusItem> items,
            boolean shouldAddLoss,
            List<AssignedItem> currentAssignedItems
    ) {
        List<Grade> grades = gradeService.getAnimalGrades(animalId);

        if (grades.isEmpty()) {
            return potentialXpMapper.toEmptyChestPotentialXpResponseDto();
        }

        List<AssignedItem> newAssignedItems = itemsToAssignedItems(new ArrayList<>(items));

        BigDecimal currentItemsBonusXp = getTotalBenefitFromBonusItems(currentAssignedItems);

        countFlatBonus(currentAssignedItems, newAssignedItems, grades);

        PotentialBonusXpResponseDto summary = getPotentialFlatBonusSummary(currentAssignedItems, newAssignedItems, currentItemsBonusXp);
        Optional<BigDecimal> lossForItem = shouldAddLoss ? Optional.of(summary.getLossXp()) : Optional.empty();
        Map<String, PotentialBonusXpResponseDto> itemDetails = potentialXpMapper.assignedItemsToItemDetails(newAssignedItems, lossForItem);

        return ChestPotentialXpResponseDto.builder()
                .summary(summary)
                .itemDetails(itemDetails)
                .build();
    }

    private List<AssignedItem> itemsToAssignedItems(List<Item> items) {
        return items.stream()
                .map(item -> AssignedItem.builder()
                        .reward(item)
                        .bonusXp(BigDecimal.valueOf(0.0))
                        .build()
                )
                .collect(Collectors.toList());
    }

    private void countFlatBonus(List<AssignedItem> assignedItems, List<AssignedItem> newAssignedItems, List<Grade> grades) {
        Map<FlatBonusItemBehavior, List<AssignedItem>> assignedItemsByBehavior = assignedRewardService.groupFlatBonusItemsByBehavior(assignedItems);
        List<AssignedItem> oneEventItems = Optional.ofNullable(assignedItemsByBehavior.get(FlatBonusItemBehavior.ONE_EVENT))
                .orElse(new ArrayList<>());
        List<AssignedItem> multipleEventsItems = Optional.ofNullable(assignedItemsByBehavior.get(FlatBonusItemBehavior.MULTIPLE_EVENTS))
                .orElse(new ArrayList<>());

        addNewItemsToRespectiveFlatBonusMap(newAssignedItems, oneEventItems, multipleEventsItems);
        Set<AssignedItem> overLimitItems = new HashSet<>();
        Map<Long, Long> currentCountById = assignedRewardService.countAssignedItemsByRewardId(assignedItems);

        assignedRewardService.handleReachedLimitItems(
                newAssignedItems,
                currentCountById,
                item -> item.getReward().getId(),
                item -> ((Item) Hibernate.unproxy(item.getReward())).getLimit(),
                (item, isOverLimit) -> {
                    if (isOverLimit) {
                        overLimitItems.add(item);
                    }
                }
        );

        oneEventItems.removeIf(item -> overLimitItems.stream().anyMatch(overItem -> overItem == item));
        multipleEventsItems.removeIf(item -> overLimitItems.stream().anyMatch(overItem -> overItem == item));

        bonusXpCalculator.countOneEventItemsBonus(oneEventItems, grades);
        bonusXpCalculator.countMultipleEventsItemsBonus(oneEventItems, multipleEventsItems, grades);


        oneEventItems.addAll(overLimitItems.stream()
                .filter(item -> ((FlatBonusItem) item.getReward()).getBehavior() == FlatBonusItemBehavior.ONE_EVENT)
                .toList());
        multipleEventsItems.addAll(overLimitItems.stream()
                .filter(item -> ((FlatBonusItem) item.getReward()).getBehavior() == FlatBonusItemBehavior.MULTIPLE_EVENTS)
                .toList());
    }

    private PotentialBonusXpResponseDto getPotentialFlatBonusSummary(List<AssignedItem> currentFlatBonusItemsMerged, List<AssignedItem> newAssignedItems, BigDecimal currentItemsBonusXp) {
        BigDecimal bonusXp = getTotalBenefitFromBonusItems(newAssignedItems);
        BigDecimal newBonusXpForCurrentItems = getTotalBenefitFromBonusItems(currentFlatBonusItemsMerged);
        BigDecimal lossXp = currentItemsBonusXp.subtract(newBonusXpForCurrentItems);

        return potentialXpMapper.toChestPotentialXpResponseDto(bonusXp, Optional.of(lossXp));
    }


    private void calculatePotentialPercentageBonusXp(Long animalId, Map<Long, BigDecimal> flatBonusByEventSection, AssignedItem assignedItem) {
        PercentageBonusItem item = (PercentageBonusItem) assignedItem.getReward();
        if (assignedRewardService.isLimitReached(item, animalId)) {
            return;
        }
        Long eventSectionId = item.getEventSection().getId();
        BigDecimal rawXp = hallOfFameService.getStudentEventSectionScoreDetails(
                        animalId,
                        eventSectionId
                )
                .getRawXp();

        BigDecimal flatBonus = flatBonusByEventSection.getOrDefault(eventSectionId, BigDecimal.valueOf(0.0));
        BigDecimal totalXp = rawXp.add(flatBonus);
        BigDecimal percentageBonus = BigDecimal.valueOf(item.getPercentageBonus());

        BigDecimal bonusXp = totalXp
                .multiply(percentageBonus)
                .divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP);

        assignedItem.setBonusXp(bonusXp);
    }


    private Map<String, PotentialBonusXpResponseDto> calculateSingleItemBonus(Long animalId, Item item) {
        return switch (item.getItemType()) {
            case FLAT_BONUS -> getPotentialFlatBonusXp(
                    List.of(item),
                    animalId,
                    true,
                    assignedRewardService.getAnimalAssignedItems(animalId)
            ).getItemDetails();

            case PERCENTAGE_BONUS -> getPotentialPercentageBonusXp(
                    List.of(item),
                    animalId,
                    true,
                    new HashMap<>()
            ).getItemDetails();
        };
    }


    private void addNewItemsToRespectiveFlatBonusMap(
            List<AssignedItem> items,
            List<AssignedItem> oneEventAssignedItems,
            List<AssignedItem> multipleEventsItems
    ) {
        for (AssignedItem item : items) {
            switch (((FlatBonusItem) (item.getReward())).getBehavior()) {
                case ONE_EVENT: {
                    oneEventAssignedItems.add(item);
                    break;
                }
                case MULTIPLE_EVENTS: {
                    multipleEventsItems.add(item);
                }
            }
        }
    }

    private BigDecimal getTotalBenefitFromBonusItems(List<AssignedItem> assignedItems) {
        return assignedItems.stream()
                .map(AssignedItem::getBonusXp)
                .reduce(BigDecimal.valueOf(0.0), BigDecimal::add);
    }
}

