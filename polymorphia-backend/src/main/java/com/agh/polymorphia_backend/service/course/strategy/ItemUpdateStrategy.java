package com.agh.polymorphia_backend.service.course.strategy;

import com.agh.polymorphia_backend.dto.request.course_import.reward.FlatBonusItemDetailsRequestDto;
import com.agh.polymorphia_backend.dto.request.course_import.reward.ItemDetailsRequestDto;
import com.agh.polymorphia_backend.dto.request.course_import.reward.PercentageBonusItemDetailsRequestDto;
import com.agh.polymorphia_backend.model.reward.FlatBonusItem;
import com.agh.polymorphia_backend.model.reward.Item;
import com.agh.polymorphia_backend.model.reward.PercentageBonusItem;
import com.agh.polymorphia_backend.repository.course.CourseRepository;
import com.agh.polymorphia_backend.repository.event_section.EventSectionRepository;
import com.agh.polymorphia_backend.repository.reward.ItemRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class ItemUpdateStrategy implements EntityUpdateStrategy<ItemDetailsRequestDto, Item> {

    private final ItemRepository itemRepository;
    private final CourseRepository courseRepository;
    private final EventSectionRepository eventSectionRepository;

    @Override
    public Function<ItemDetailsRequestDto, String> getKeyExtractor() {
        return ItemDetailsRequestDto::getKey;
    }

    @Override
    public Function<Item, String> getEntityKeyExtractor() {
        return Item::getKey;
    }

    @Override
    public Function<ItemDetailsRequestDto, ?> getTypeExtractor() {
        return ItemDetailsRequestDto::getType;
    }

    @Override
    public JpaRepository<Item, Long> getRepository() {
        return itemRepository;
    }

    @Override
    public List<Item> findAllByKeys(List<String> keys, Long courseId) {
        List<Item> itemsFromKeys = itemRepository.findAllByKeyIn(keys, courseId);
        Map<String, Item> itemByKey = itemsFromKeys.stream()
                .collect(Collectors.toMap(Item::getKey, item -> item));

        return keys.stream()
                .map(itemByKey::get)
                .collect(Collectors.toList());
    }

    @Override
    public Item createNewEntity(ItemDetailsRequestDto dto) {
        return switch (dto.getType()) {
            case FLAT_BONUS -> new FlatBonusItem();
            case PERCENTAGE_BONUS -> new PercentageBonusItem();
        };
    }

    @Override
    public Item updateEntity(Item entity, ItemDetailsRequestDto dto,
                             Map<ItemDetailsRequestDto, Long> orderIds, Long courseId) {
        Long eventSectionId = eventSectionRepository.findIdByKeyAndCourseId(dto.getEventSectionKey(), courseId);
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setKey(dto.getKey());
        entity.setOrderIndex(orderIds.get(dto));
        entity.setLimit(dto.getLimit());
        entity.setImageUrl(dto.getImageUrl());
        entity.setEventSection(eventSectionRepository.getReferenceById(eventSectionId));
        if (entity.getId() == null) {
            entity.setCourse(courseRepository.getReferenceById(courseId));
        }

        switch (dto.getType()) {
            case FLAT_BONUS: {
                ((FlatBonusItem) entity).setXpBonus(((FlatBonusItemDetailsRequestDto) dto).getXpBonus());
                ((FlatBonusItem) entity).setBehavior(((FlatBonusItemDetailsRequestDto) dto).getBehavior());
                break;
            }
            case PERCENTAGE_BONUS: {
                ((PercentageBonusItem) entity).setPercentageBonus(((PercentageBonusItemDetailsRequestDto) dto).getPercentageBonus());
                break;
            }
        }

        return entity;
    }
}