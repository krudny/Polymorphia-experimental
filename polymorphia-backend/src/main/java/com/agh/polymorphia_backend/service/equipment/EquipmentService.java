package com.agh.polymorphia_backend.service.equipment;

import com.agh.polymorphia_backend.dto.request.equipment.EquipmentChestOpenRequestDto;
import com.agh.polymorphia_backend.dto.response.equipment.EquipmentChestResponseDto;
import com.agh.polymorphia_backend.dto.response.equipment.EquipmentItemResponseDto;
import com.agh.polymorphia_backend.dto.response.equipment.potential_xp.ChestPotentialXpResponseDtoWithType;
import com.agh.polymorphia_backend.dto.response.reward.BaseRewardResponseDto;
import com.agh.polymorphia_backend.dto.response.reward.chest.ChestResponseDtoBase;
import com.agh.polymorphia_backend.dto.response.reward.item.ItemResponseDtoBase;
import com.agh.polymorphia_backend.model.reward.Chest;
import com.agh.polymorphia_backend.model.reward.Item;
import com.agh.polymorphia_backend.model.reward.assigned.AssignedChest;
import com.agh.polymorphia_backend.model.reward.assigned.AssignedItem;
import com.agh.polymorphia_backend.model.reward.assigned.AssignedReward;
import com.agh.polymorphia_backend.model.reward.chest.ChestBehavior;
import com.agh.polymorphia_backend.model.reward.chest.ChestFilterEnum;
import com.agh.polymorphia_backend.model.user.UserType;
import com.agh.polymorphia_backend.repository.reward.ChestRepository;
import com.agh.polymorphia_backend.repository.reward.ItemRepository;
import com.agh.polymorphia_backend.service.mapper.AssignedRewardMapper;
import com.agh.polymorphia_backend.service.mapper.RewardMapper;
import com.agh.polymorphia_backend.service.reward.AssignedRewardService;
import com.agh.polymorphia_backend.service.reward.BonusXpCalculator;
import com.agh.polymorphia_backend.service.reward.PotentialBonusXpCalculator;
import com.agh.polymorphia_backend.service.student.AnimalService;
import com.agh.polymorphia_backend.service.user.UserService;
import com.agh.polymorphia_backend.service.validation.AccessAuthorizer;
import lombok.AllArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.agh.polymorphia_backend.service.user.UserService.INVALID_ROLE;

@Service
@AllArgsConstructor
public class EquipmentService {
    private final AccessAuthorizer accessAuthorizer;
    private final UserService userService;
    private final AnimalService animalService;
    private final AssignedRewardService assignedRewardService;
    private final AssignedRewardMapper assignedRewardMapper;
    private final BonusXpCalculator bonusXpCalculator;
    private final ChestRepository chestRepository;
    private final RewardMapper rewardMapper;
    private final ItemRepository itemRepository;
    private final PotentialBonusXpCalculator potentialBonusXpCalculator;

    // TODO: performance + sql
    public List<EquipmentItemResponseDto> getEquipmentItems(Long courseId, Optional<Long> studentId) {
        Long animalId = getAnimalIdAndValidatePermissions(courseId, studentId);
        List<AssignedItem> assignedItems = assignedRewardService.getAnimalAssignedItems(animalId);
        List<Long> assignedItemIds = getAssignedRewardsIds(assignedItems);
        List<Item> remainingCourseItems = itemRepository.findAllByCourseIdAndItemIdNotIn(courseId, assignedItemIds);

        return Stream.concat(
                        assignedRewardMapper.assignedItemsToResponseDto(assignedItems, animalId).stream()
                                .sorted(Comparator.comparing(response -> response.getBase().getOrderIndex())),
                        rewardMapper.itemsToEquipmentResponseDto(remainingCourseItems, animalId).stream()
                                .sorted(Comparator.comparing(response -> response.getBase().getOrderIndex()))
                )
                .toList();
    }

    // TODO: performance + sql
    public List<EquipmentChestResponseDto> getEquipmentChests(Long courseId, Optional<Long> studentId) {
        Long animalId = getAnimalIdAndValidatePermissions(courseId, studentId);
        List<AssignedChest> assignedChests = assignedRewardService.getAnimalAssignedChests(animalId, ChestFilterEnum.ALL);
        List<Long> assignedChestIds = getAssignedRewardsIds(assignedChests);
        List<Chest> remainingCourseChests = chestRepository.findAllByCourseIdAndChestIdNotIn(courseId, assignedChestIds);
        List<EquipmentChestResponseDto> assignedChestsResponse = assignedRewardMapper.assignedChestsToResponseDto(assignedChests, animalId);
        setIsLimitReachedForALLChests(assignedChestsResponse, animalId);

        return Stream.concat(
                        assignedChestsResponse.stream()
                                .sorted(Comparator.comparing((EquipmentChestResponseDto response) -> response.getDetails().getReceivedDate())
                                        .thenComparing(response -> response.getBase().getOrderIndex())),
                        rewardMapper.chestsToEquipmentResponseDto(remainingCourseChests, animalId).stream()
                                .sorted(Comparator.comparing(response -> response.getBase().getOrderIndex()))
                )
                .toList();
    }

    private Long getAnimalIdAndValidatePermissions(Long courseId, Optional<Long> studentId) {
        UserType userType = userService.getCurrentUserRole();
        if (studentId.isEmpty() && UserType.STUDENT.equals(userType)) {
            return animalService.validateAndGetAnimalId(courseId);
        } else if (studentId.isPresent() && !UserType.STUDENT.equals(userType)) {
            accessAuthorizer.authorizeStudentDataAccess(courseId, studentId.get());
            return animalService.getAnimalId(studentId.get(), courseId);
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, INVALID_ROLE);
    }

    public ChestPotentialXpResponseDtoWithType getPotentialXpForChest(Long courseId, Long assignedChestId) {
        Long animalId = animalService.getAnimalIdForAssignedChest(assignedChestId);
        AssignedChest assignedChest = assignedRewardService.getUnopenedAssignedChestByIdWithoutLock(assignedChestId);
        validatePermissions(courseId, animalId);

        Chest chest = (Chest) Hibernate.unproxy(assignedChest.getReward());

        return switch (chest.getBehavior()) {
            case ALL -> potentialBonusXpCalculator.getPotentialBonusXpForALLChest(animalId, chest);
            case ONE_OF_MANY -> potentialBonusXpCalculator.getPotentialBonusXpForONEChest(animalId, chest);
        };
    }

    @Transactional
    public void openChest(Long courseId, EquipmentChestOpenRequestDto requestDto) {
        Long animalId = animalService.getAnimalIdForAssignedChest(requestDto.getAssignedChestId());
        AssignedChest assignedChest = assignedRewardService.getUnopenedAssignedChestByIdWithLock(requestDto.getAssignedChestId());
        validatePermissions(courseId, animalId);

        ZonedDateTime openDate = ZonedDateTime.now();
        Chest chest = (Chest) Hibernate.unproxy(assignedChest.getReward());

        List<AssignedItem> assignedItems = switch (chest.getBehavior()) {
            case ONE_OF_MANY -> createAssignedItemsFromRequest(requestDto, assignedChest, openDate, animalId);
            case ALL -> createAssignedItemsFromAssignedChest(animalId, chest, assignedChest, openDate);
        };

        assignedChest.setIsUsed(true);
        assignedChest.setUsedDate(openDate);
        assignedChest.getAssignedItems().addAll(assignedItems);

        assignedRewardService.saveAssignedChest(List.of(assignedChest));
        assignedRewardService.saveAssignedItems(assignedItems);
        bonusXpCalculator.updateAnimalFlatBonusXp(animalId);
        bonusXpCalculator.updateAnimalPercentageBonusXp(animalId);
    }

    private void validatePermissions(Long courseId, Long animalId) {
        switch (userService.getCurrentUserRole()) {
            case STUDENT:
                Long currentAnimalId = animalService.validateAndGetAnimalId(courseId);
                if (!currentAnimalId.equals(animalId)) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Brak dostępu.");
                }
                break;
            case INSTRUCTOR:
            case COORDINATOR:
                Long studentId = animalService.getStudentIdForAnimalId(animalId);
                accessAuthorizer.authorizeStudentDataAccess(courseId, studentId);
                break;
            default:
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, INVALID_ROLE);
        }
    }

    private List<Long> getAssignedRewardsIds(List<? extends AssignedReward> assignedRewards) {
        return assignedRewards.stream()
                .map(assignedReward -> assignedReward.getReward().getId())
                .toList();
    }

    private List<AssignedItem> createAssignedItemsFromRequest(EquipmentChestOpenRequestDto requestDto, AssignedChest assignedChest, ZonedDateTime openDate, Long animalId) {
        Chest chest = (Chest) Hibernate.unproxy(assignedChest.getReward());
        if (requestDto.getItemId() == null) {
            List<AssignedItem> assignedItems = createNewAssignedItemsFromChest(chest, assignedChest, openDate, animalId);

            if (!assignedItems.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            }

            return new ArrayList<>();
        }

        Item item = chest.getItems().stream()
                .map(i -> (Item) Hibernate.unproxy(i))
                .filter(i -> i.getId().equals(requestDto.getItemId()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nie znaleziono przedmiotu w skrzyni."));

        if (assignedRewardService.isLimitReached(item, animalId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Osiągnięto maksymalną liczbę przedmiotów.");
        }

        return List.of(
                assignedRewardService.createAssignedItem(Optional.of(assignedChest), assignedChest.getCriterionGrade(), item, openDate)
        );
    }

    private List<AssignedItem> createAssignedItemsFromAssignedChest(
            Long animalId,
            Chest chest,
            AssignedChest assignedChest,
            ZonedDateTime openDate
    ) {
        List<AssignedItem> newAssignedItems = createNewAssignedItemsFromChest(chest, assignedChest, openDate, animalId);

        List<AssignedItem> currentAssignedItems = assignedRewardService.getAnimalAssignedItems(animalId);
        Map<Long, Long> currentCountById = assignedRewardService.countAssignedItemsByRewardId(currentAssignedItems);
        Set<AssignedItem> itemsToRemove = Collections.newSetFromMap(new IdentityHashMap<>());
        assignedRewardService.handleReachedLimitItems(
                newAssignedItems,
                currentCountById,
                item -> item.getReward().getId(),
                item -> ((Item) item.getReward()).getLimit(),
                (item, isOverLimit) -> {
                    if (isOverLimit) {
                        itemsToRemove.add(item);
                    }
                }
        );

        newAssignedItems.removeIf(itemsToRemove::contains);

        return newAssignedItems;
    }

    private List<AssignedItem> createNewAssignedItemsFromChest(Chest chest, AssignedChest assignedChest, ZonedDateTime openDate, Long animalId) {
        return chest
                .getItems().stream()
                .map(i -> (Item) Hibernate.unproxy(i))
                .filter(i -> !assignedRewardService.isLimitReached(i, animalId))
                .map(item -> assignedRewardService.createAssignedItem(Optional.of(assignedChest), assignedChest.getCriterionGrade(), item, openDate))
                .collect(Collectors.toList());
    }


    private void setIsLimitReachedForALLChests(List<EquipmentChestResponseDto> assignedChestsResponse, Long animalId) {
        List<AssignedItem> animalAssignedItems = assignedRewardService.getAnimalAssignedItems(animalId);

        Map<Long, Long> currentCountById = assignedRewardService.countAssignedItemsByRewardId(animalAssignedItems);

        assignedChestsResponse.forEach(assignedChest -> {
            ChestResponseDtoBase chest = (ChestResponseDtoBase) assignedChest.getBase();
            if (chest.getBehavior().equals(ChestBehavior.ALL)) {
                assignedRewardService.handleReachedLimitItems(
                        chest.getChestItems(),
                        currentCountById,
                        BaseRewardResponseDto::getId,
                        item -> ((ItemResponseDtoBase) item).getLimit(),
                        (item, isOverLimit) ->
                                ((ItemResponseDtoBase) item).setIsLimitReached(isOverLimit)
                );
            }
        });
    }
}

