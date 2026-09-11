package com.agh.polymorphia_backend.controller;

import com.agh.polymorphia_backend.dto.request.hall_of_fame.HallOfFameRequestDto;
import com.agh.polymorphia_backend.model.hall_of_fame.SearchBy;
import com.agh.polymorphia_backend.model.hall_of_fame.SortOrder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.agh.polymorphia_backend.controller.ControllerTestUtil.*;
import static io.restassured.path.json.JsonPath.from;
import static org.junit.jupiter.api.Assertions.assertEquals;

class HallOfFameControllerTest extends ControllerTestConfig {
    private static final String RESOURCE_BASE_PATH = "responses/hall_of_fame/";

    private static Resource getResource(String fileName) {
        return new ClassPathResource(RESOURCE_BASE_PATH + fileName + ".json");
    }

    private static HallOfFameRequestDto.HallOfFameRequestDtoBuilder baseRequestBuilder() {
        return HallOfFameRequestDto.builder()
                .courseId(1L)
                .page(0)
                .size(10)
                .groups(List.of("BM-SR13"))
                .searchBy(SearchBy.ANIMAL_NAME)
                .searchTerm("hof_");
    }

    private static Stream<Arguments> hallOfFameSource() {
        return Stream.of(
                Arguments.of("animalName", SortOrder.ASC, "sortByAnimalNameAsc", "sort by animalName asc"),
                Arguments.of("bonuses", SortOrder.DESC, "sortByBonusesDesc", "sort by bonuses desc"),
                Arguments.of("total", SortOrder.DESC, "sortByTotalDesc", "sort by total desc"),
                Arguments.of("total", SortOrder.ASC, "sortByTotalAsc", "sort by total asc"),
                Arguments.of("Lab", SortOrder.DESC, "sortByFirstEventSectionDesc", "sort by \"Lab\" desc"),
                Arguments.of("Lab", SortOrder.ASC, "sortByFirstEventSectionAsc", "sort by \"Lab\" asc"),
                Arguments.of("Kartkówka", SortOrder.DESC, "sortBySecondEventSectionDesc", "sort by \"Kartkówka\" desc")
        );
    }

    private static Stream<Arguments> hallOfFameSource_ForInstructor() {
        return Stream.of(
                Arguments.of("studentName", SortOrder.ASC, "sortByStudentNameAsc", "sort by studentName asc"),
                Arguments.of("studentName", SortOrder.DESC, "sortByStudentNameDesc", "sort by studentName desc")
        );
    }

    private static Stream<Arguments> hallOfFameSource_currentPage() {
        return Stream.of(
                Arguments.of("animalName", SortOrder.ASC, 0, "sort by animalName asc"),
                Arguments.of("animalName", SortOrder.DESC, 2, "sort by animalName desc"),
                Arguments.of("total", SortOrder.DESC, 1, "sort by total desc"),
                Arguments.of("bonuses", SortOrder.DESC, 0, "sort by bonus desc"),
                Arguments.of("Lab", SortOrder.DESC, 1, "sort by \"Lab\" desc"),
                Arguments.of("Kartkówka", SortOrder.DESC, 2, "sort by \"Kartkówka\" desc"),
                Arguments.of("Kartkówka", SortOrder.ASC, 0, "sort by \"Kartkówka\" desc")
        );
    }

    @Test
    void getPodium_shouldReturnPodium_ForStudent() throws IOException {
        String actualResponse = getEndpoint("/hall-of-fame/podium?courseId={courseId}",
                "student@agh.com", "password", 200, 1);

        assertJsonEquals(getResource("podium"), actualResponse);
    }

    @Test
    void getPodium_shouldReturnPodium_forInstructor() throws IOException {
        String actualResponse = getEndpoint("/hall-of-fame/podium?courseId={courseId}",
                "instructor2@agh.com", "password", 200, 1);

        assertJsonEquals(getResource("podiumInstructor"), actualResponse);
    }

    @ParameterizedTest(name = "{3}")
    @MethodSource("hallOfFameSource")
    void getHallOfFame_shouldReturnHalOfFame_forStudent(String sortBy, SortOrder sortOrder, String fileName, String testName) throws IOException {
        HallOfFameRequestDto requestDto = baseRequestBuilder().sortBy(sortBy).sortOrder(sortOrder).build();
        String actualResponse = postEndpoint("/hall-of-fame", "anowak@agh.com",
                "password", 200, Optional.of(requestDto));

        assertJsonEquals(getResource(fileName), actualResponse);
    }

    @ParameterizedTest(name = "{3}")
    @MethodSource({"hallOfFameSource", "hallOfFameSource_ForInstructor"})
    void getHallOfFame_shouldReturnHalOfFame_ForInstructor(String sortBy, SortOrder sortOrder, String fileName, String testName) throws IOException {
        HallOfFameRequestDto requestDto = baseRequestBuilder().sortBy(sortBy).sortOrder(sortOrder).build();
        String actualResponse = postEndpoint("/hall-of-fame", "instructor2@agh.com",
                "password", 200, Optional.of(requestDto));

        assertJsonEquals(getResource(fileName + "Instructor"), actualResponse);
    }

    @Test
    void getHallOfFame_shouldReturnHalOfFame_SearchByStudentName() throws IOException {
        HallOfFameRequestDto requestDto = baseRequestBuilder()
                .searchBy(SearchBy.STUDENT_NAME)
                .searchTerm("and")
                .sortBy("studentName")
                .sortOrder(SortOrder.DESC)
                .build();
        String actualResponse = postEndpoint("/hall-of-fame", "instructor2@agh.com",
                "password", 200, Optional.of(requestDto));

        assertJsonEquals(getResource("searchByStudentName"), actualResponse);
    }

    @ParameterizedTest(name = "{3}")
    @MethodSource("hallOfFameSource_currentPage")
    void getHallOfFame_shouldReturnValidCurrentPage(String sortBy, SortOrder sortOrder, int expectedUserPage, String testName) {
        HallOfFameRequestDto requestDto = baseRequestBuilder()
                .sortBy(sortBy)
                .sortOrder(sortOrder)
                .size(2)
                .build();
        String actualResponse = postEndpoint("/hall-of-fame", "anowak@agh.com",
                "password", 200, Optional.of(requestDto));
        int actualUserPage = from(actualResponse).getInt("currentUserPage");

        assertEquals(expectedUserPage, actualUserPage);
    }

    @Test
    void getHallOfFame_shouldReturnError() {
        HallOfFameRequestDto hof = baseRequestBuilder()
                .sortBy("some-not-existing-sort-by")
                .sortOrder(SortOrder.DESC)
                .build();

        postEndpoint("/hall-of-fame", "student@agh.com", "password", 400, Optional.of(hof));
    }
}
