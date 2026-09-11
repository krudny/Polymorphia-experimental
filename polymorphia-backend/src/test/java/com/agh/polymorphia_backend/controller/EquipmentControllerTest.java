package com.agh.polymorphia_backend.controller;

import com.agh.polymorphia_backend.dto.request.equipment.EquipmentChestOpenRequestDto;
import com.google.ortools.Loader;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.test.annotation.Rollback;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static com.agh.polymorphia_backend.controller.ControllerTestUtil.*;

public class EquipmentControllerTest extends ControllerTestConfig {
    private static final List<String> datePropertyNames = List.of("receivedDate", "usedDate");

    static {
        Loader.loadNativeLibraries();
    }

    @Value("classpath:responses/equipment/items.json")
    private Resource itemsJson;
    @Value("classpath:responses/equipment/chests.json")
    private Resource chestsJson;
    @Value("classpath:responses/equipment/chest_all_opened.json")
    private Resource chestALLOpenedJson;
    @Value("classpath:responses/equipment/items_all_opened.json")
    private Resource itemsALLOpenedJson;
    @Value("classpath:responses/equipment/chest_one_percent_opened.json")
    private Resource chestONEPercentageOpenedJson;
    @Value("classpath:responses/equipment/items_one_percent_opened.json")
    private Resource itemsONEPercentageOpenedJson;
    @Value("classpath:responses/equipment/potential_xp_all.json")
    private Resource potentialXpALLJson;
    @Value("classpath:responses/equipment/potential_xp_one.json")
    private Resource potentialXpONEJson;

    @Test
    @Rollback
    void getItems_ShouldReturnAnimalItems() throws IOException {
        String actualResponse = getEndpoint("/equipment/items?courseId={courseId}",
                "sampleuser@test.com", "password", 200, 4);

        assertJsonEquals(itemsJson, actualResponse);
    }

    @Test
    @Rollback
    void getChests_ShouldReturnAnimalChests() throws IOException {
        String actualResponse = getEndpoint("/equipment/chests?courseId={courseId}",
                "sampleuser@test.com", "password", 200, 4);

        assertJsonEquals(chestsJson, actualResponse);
    }

    @Test
    @Rollback
    void openChestONEFlatBonusLimitReached_ShouldThrowBadRequest() {
        EquipmentChestOpenRequestDto requestDto = EquipmentChestOpenRequestDto.builder()
                .assignedChestId(7L)
                .itemId(9L)
                .build();

        postEndpoint("/equipment/chests/open?courseId={courseId}",
                "sampleuser@test.com", "password", 400, Optional.of(requestDto), 4);

    }

    @Test
    @Rollback
    void openChestALL_ShouldOpenChestAndRecalculateBonuses() throws IOException {
        EquipmentChestOpenRequestDto requestDto = EquipmentChestOpenRequestDto.builder()
                .assignedChestId(6L)
                .build();

        postEndpoint("/equipment/chests/open?courseId={courseId}",
                "sampleuser@test.com", "password", 204, Optional.of(requestDto), 4);

        String chests = getEndpoint("/equipment/chests?courseId={courseId}",
                "sampleuser@test.com", "password", 200, 4);

        assertJsonEqualsIgnoringDates(chestALLOpenedJson, chests, datePropertyNames);

        String items = getEndpoint("/equipment/items?courseId={courseId}",
                "sampleuser@test.com", "password", 200, 4);

        assertJsonEqualsIgnoringDates(itemsALLOpenedJson, items, datePropertyNames);
    }

    @Test
    @Rollback
    void openChestONENullItem_ShouldThrowBadRequest() {
        EquipmentChestOpenRequestDto requestDto = EquipmentChestOpenRequestDto.builder()
                .assignedChestId(7L)
                .build();

        postEndpoint("/equipment/chests/open?courseId={courseId}",
                "sampleuser@test.com", "password", 400, Optional.of(requestDto), 4);
    }

    @Test
    @Rollback
    void openChestONEPercentageBonus_ShouldOpenChestAndRecalculateBonuses() throws IOException {
        EquipmentChestOpenRequestDto requestDto = EquipmentChestOpenRequestDto.builder()
                .assignedChestId(7L)
                .itemId(8L)
                .build();

        postEndpoint("/equipment/chests/open?courseId={courseId}",
                "sampleuser@test.com", "password", 204, Optional.of(requestDto), 4);

        String chests = getEndpoint("/equipment/chests?courseId={courseId}",
                "sampleuser@test.com", "password", 200, 4);

        assertJsonEqualsIgnoringDates(chestONEPercentageOpenedJson, chests, datePropertyNames);

        String items = getEndpoint("/equipment/items?courseId={courseId}",
                "sampleuser@test.com", "password", 200, 4);

        assertJsonEqualsIgnoringDates(itemsONEPercentageOpenedJson, items, datePropertyNames);
    }

    @Test
    @Rollback
    void openChestONEFlatBonusLimitReached_ShouldLetOpen() throws IOException {
        EquipmentChestOpenRequestDto requestDto = EquipmentChestOpenRequestDto.builder()
                .assignedChestId(12L)
                .build();

        postEndpoint("/equipment/chests/open?courseId={courseId}",
                "sampleuser@test.com", "password", 404, Optional.of(requestDto), 4);

        String chests = getEndpoint("/equipment/chests?courseId={courseId}",
                "sampleuser@test.com", "password", 200, 4);

        assertJsonEqualsIgnoringDates(chestsJson, chests, datePropertyNames);

        String items = getEndpoint("/equipment/items?courseId={courseId}",
                "sampleuser@test.com", "password", 200, 4);

        assertJsonEqualsIgnoringDates(itemsJson, items, datePropertyNames);
    }

    @Test
    @Rollback
    void potentialXpALL_ShouldReturnPotentialXp() throws IOException {
        String chests = getEndpoint("/equipment/chests/potential-xp?courseId={courseId}&assignedChestId={chestId}",
                "sampleuser@test.com", "password", 200, 4, 6L);

        assertJsonEquals(potentialXpALLJson, chests);
    }

    @Test
    @Rollback
    void potentialXpONE_ShouldReturnPotentialXp() throws IOException {
        String chests = getEndpoint("/equipment/chests/potential-xp?courseId={courseId}&assignedChestId={chestId}",
                "sampleuser@test.com", "password", 200, 4, 7L);

        assertJsonEquals(potentialXpONEJson, chests);
    }
}
