package com.agh.polymorphia_backend.service.reward;

import com.agh.polymorphia_backend.dto.request.reward.ShortAssignedRewardRequestDto;
import com.agh.polymorphia_backend.model.criterion.CriterionGrade;
import com.agh.polymorphia_backend.model.reward.Chest;
import com.agh.polymorphia_backend.model.reward.FlatBonusItem;
import com.agh.polymorphia_backend.model.reward.Item;
import com.agh.polymorphia_backend.model.reward.Reward;
import com.agh.polymorphia_backend.model.reward.assigned.AssignedChest;
import com.agh.polymorphia_backend.model.reward.assigned.AssignedItem;
import com.agh.polymorphia_backend.model.reward.assigned.AssignedReward;
import com.agh.polymorphia_backend.model.reward.chest.ChestFilterEnum;
import com.agh.polymorphia_backend.model.reward.item.FlatBonusItemBehavior;
import com.agh.polymorphia_backend.model.reward.item.ItemType;
import com.agh.polymorphia_backend.model.user.UserType;
import com.agh.polymorphia_backend.repository.reward.assigned.AssignedChestRepository;
import com.agh.polymorphia_backend.repository.reward.assigned.AssignedItemRepository;
import com.agh.polymorphia_backend.service.student.AnimalService;
import com.agh.polymorphia_backend.service.user.UserService;
import lombok.AllArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@AllArgsConstructor
public class AssignedRewardService {
    private static final String UNOPENED_ASSIGNED_CHEST_NOT_FOUND = "Nie znaleziono nieotwartej skrzynki, przypisanej do zwierzaka, o podanym ID.";
    private final AssignedChestRepository assignedChestRepository;
    private final AssignedItemRepository assignedItemRepository;
    private final AnimalService animalService;
    private final UserService userService;
    private final RewardService rewardService;

    public List<AssignedChest> getAnimalAssignedChests(Long animalId, ChestFilterEnum chestFilter) {
        return assignedChestRepository.findAnimalAssignedChests(animalId, chestFilter.toString());
    }

    public List<AssignedItem> getAnimalAssignedItems(Long animalId) {
        return assignedItemRepository.findAnimalAssignedItems(animalId);
    }

    public List<AssignedItem> getAnimalAssignedItemsWithoutCriterionItems(Long animalId, Long criterionId) {
        return assignedItemRepository.findAnimalAssignedItemsWithoutCriterionItems(animalId, criterionId);
    }

    public Map<FlatBonusItemBehavior, List<AssignedItem>> groupFlatBonusItemsByBehavior(List<AssignedItem> assignedItems) {
        return filterAssignedItemsByType(assignedItems, ItemType.FLAT_BONUS).stream()
                .sorted(Comparator.comparing(AssignedReward::getReceivedDate))
                .collect(Collectors.groupingBy(
                        assignedItem -> ((FlatBonusItem) Hibernate.unproxy(assignedItem.getReward())).getBehavior(),
                        Collectors.toList()
                ));
    }

    public List<AssignedItem> getAnimalAssignedItemsByType(Long animalId, ItemType itemType) {
        return filterAssignedItemsByType(getAnimalAssignedItems(animalId), itemType);

    }

    public Map<Long, BigDecimal> getTotalBonusByEventSection(List<AssignedItem> assignedItems) {
        Map<Long, BigDecimal> totalBonusByEventSection = new HashMap<>();
        for (AssignedItem assignedItem : assignedItems) {
            Long eventSectionId = ((Item) Hibernate.unproxy(assignedItem.getReward()))
                    .getEventSection()
                    .getId();

            BigDecimal bonusXp = assignedItem.getBonusXp();

            totalBonusByEventSection.merge(eventSectionId, bonusXp, BigDecimal::add);
        }

        return totalBonusByEventSection;
    }

    public AssignedChest getUnopenedAssignedChestByIdWithoutLock(Long assignedChestId) {
        return assignedChestRepository
                .findNotUsedAssignedChestsById(assignedChestId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        UNOPENED_ASSIGNED_CHEST_NOT_FOUND
                ));
    }

    public AssignedChest getUnopenedAssignedChestByIdWithLock(Long assignedChestId) {
        return assignedChestRepository
                .findNotUsedAssignedChestsByIdWithLock(assignedChestId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        UNOPENED_ASSIGNED_CHEST_NOT_FOUND
                ));
    }

    public List<AssignedReward> fetchOrCreateAssignedRewards(ShortAssignedRewardRequestDto request, CriterionGrade criterionGrade) {
        Reward reward = rewardService.findById(request.getRewardId());

        return switch (reward.getRewardType()) {
            case ITEM -> regulateAssignedItemsQuantity(
                    request.getQuantity(),
                    criterionGrade,
                    reward.getId(),
                    assignedItemRepository::findByCriterionGrade,
                    () -> createAssignedItem(
                            Optional.empty(),
                            criterionGrade,
                            (Item) Hibernate.unproxy(reward),
                            ZonedDateTime.now()
                    )
            );
            case CHEST -> regulateAssignedItemsQuantity(
                    request.getQuantity(),
                    criterionGrade,
                    reward.getId(),
                    assignedChestRepository::findByCriterionGrade,
                    () -> createAssignedChest((Chest) Hibernate.unproxy(reward), criterionGrade, ZonedDateTime.now())
            );
        };
    }

    public AssignedItem createAssignedItem(Optional<AssignedChest> assignedChest, CriterionGrade criterionGrade, Item item, ZonedDateTime receivedDate) {
        return AssignedItem.builder()
                .reward(item)
                .assignedChest(assignedChest.orElse(null))
                .reward(item)
                .criterionGrade(criterionGrade)
                .isUsed(false)
                .bonusXp(BigDecimal.valueOf(0.0))
                .receivedDate(receivedDate)
                .build();
    }

    public AssignedChest createAssignedChest(Chest chest, CriterionGrade criterionGrade, ZonedDateTime receivedDate) {
        return AssignedChest.builder()
                .reward(chest)
                .isUsed(false)
                .receivedDate(receivedDate)
                .criterionGrade(criterionGrade)
                .build();
    }


    public boolean isLimitReached(Item item, Long animalId) {
        return getCurrentItemCount(animalId, item, Optional.empty()) >= item.getLimit();
    }

    public boolean willLimitBeCrossedWithNewItems(Item item, Long studentId, Integer newItemsQuantity, Long criterionIdToIgnore) {
        UserType userRole = userService.getAnyUserRoleInCourse(item.getCourse().getId(), userService.findById(studentId).getId());
        if (userRole.equals(UserType.COORDINATOR) || userRole.equals(UserType.INSTRUCTOR)) {
            return false;
        }
        Long animalId = animalService.getAnimalId(studentId, item.getCourse().getId());
        return (getCurrentItemCount(animalId, item, Optional.of(criterionIdToIgnore)) + newItemsQuantity) > item.getLimit();
    }

    public Long getCurrentItemCount(Long animalId, Item item, Optional<Long> criterionIdToIgnore) {
        List<AssignedItem> animalAssignedItems;
        if (criterionIdToIgnore.isPresent()) {
            animalAssignedItems = getAnimalAssignedItemsWithoutCriterionItems(animalId, criterionIdToIgnore.get());
        } else {
            animalAssignedItems = getAnimalAssignedItems(animalId);
        }

        return countAssignedItemsByRewardId(animalAssignedItems).getOrDefault(item.getId(), 0L);
    }


    public Map<Reward, List<AssignedItem>> groupAssignedItemsByReward(List<AssignedItem> assignedItems) {
        return assignedItems.stream()
                .collect(Collectors.groupingBy(AssignedItem::getReward));
    }

    public Map<Reward, Long> countAssignedItemsByReward(List<AssignedItem> assignedItems) {
        return assignedItems.stream()
                .collect(Collectors.groupingBy(AssignedItem::getReward, Collectors.counting()));
    }

    public Map<Long, Long> countAssignedItemsByRewardId(List<AssignedItem> assignedItems) {
        return assignedItems.stream()
                .collect(Collectors.groupingBy(item -> item.getReward().getId(), Collectors.counting()));
    }

    public <T> void handleReachedLimitItems(
            List<T> items,
            Map<Long, Long> currentCountById,
            Function<T, Long> idExtractor,
            Function<T, Integer> limitExtractor,
            BiConsumer<T, Boolean> isLimitReachedSetter
    ) {
        Map<Long, Long> itemCountById = items.stream()
                .collect(Collectors.groupingBy(idExtractor, Collectors.counting()));

        for (int i = items.size() - 1; i >= 0; i--) {
            T item = items.get(i);
            Long itemId = idExtractor.apply(item);
            Integer limit = limitExtractor.apply(item);
            long itemCount = itemCountById.getOrDefault(itemId, 0L)
                    + currentCountById.getOrDefault(itemId, 0L);

            if (itemCount > limit) {
                isLimitReachedSetter.accept(item, true);
                itemCountById.put(itemId, Math.max(itemCountById.getOrDefault(itemId, 0L) - 1, 0L));
            }
        }
    }


    public void saveAssignedChest(List<AssignedChest> chests) {
        assignedChestRepository.saveAll(chests);
    }

    public void saveAssignedItems(List<AssignedItem> items) {
        assignedItemRepository.saveAll(items);
    }

    public List<AssignedItem> filterAssignedItemsByType(List<AssignedItem> assignedItems, ItemType itemType) {
        return assignedItems.stream()
                .filter(item -> ((Item) Hibernate.unproxy(item.getReward())).getItemType().equals(itemType))
                .toList();
    }

    public List<AssignedItem> getAnimalEventSectionAssignedItems(Long animalId, Long eventSectionId) {
        List<AssignedChest> openedChests = getAnimalAssignedChests(animalId, ChestFilterEnum.USED);

        return openedChests.stream()
                .flatMap(chest -> chest.getAssignedItems().stream())
                .filter(assignedItem -> ((Item) Hibernate.unproxy(assignedItem.getReward()))
                        .getEventSection()
                        .getId()
                        .equals(eventSectionId)
                )
                .toList();
    }

    public List<AssignedReward> getCriterionGradeAssignedRewards(CriterionGrade criterionGrade) {
        return Stream.of(
                        assignedItemRepository.findByCriterionGrade(criterionGrade),
                        assignedChestRepository.findByCriterionGrade(criterionGrade)
                )
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    private <T extends AssignedReward> List<AssignedReward> regulateAssignedItemsQuantity(
            int quantity,
            CriterionGrade criterionGrade,
            Long rewardId,
            Function<CriterionGrade, List<T>> assignedRewardFetcher,
            Supplier<T> creator
    ) {
        List<AssignedReward> rewards = assignedRewardFetcher.apply(criterionGrade).stream()
                .filter(r -> r.getReward().getId().equals(rewardId))
                .collect(Collectors.toList());

        while (rewards.size() < quantity) {
            rewards.add(creator.get());
        }

        if (rewards.size() > quantity) {
            return rewards.subList(0, quantity);
        }

        return rewards;
    }
}
