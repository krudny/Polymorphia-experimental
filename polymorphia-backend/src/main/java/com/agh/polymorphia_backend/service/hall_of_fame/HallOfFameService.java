package com.agh.polymorphia_backend.service.hall_of_fame;

import com.agh.polymorphia_backend.dto.request.hall_of_fame.HallOfFameRequestDto;
import com.agh.polymorphia_backend.dto.response.hall_of_fame.HallOfFameRecordDto;
import com.agh.polymorphia_backend.dto.response.hall_of_fame.HallOfFameResponseDto;
import com.agh.polymorphia_backend.model.event_section.EventSection;
import com.agh.polymorphia_backend.model.hall_of_fame.*;
import com.agh.polymorphia_backend.model.user.UserType;
import com.agh.polymorphia_backend.model.user.student.Animal;
import com.agh.polymorphia_backend.repository.event_section.EventSectionRepository;
import com.agh.polymorphia_backend.repository.hall_of_fame.HallOfFameRepository;
import com.agh.polymorphia_backend.repository.hall_of_fame.StudentScoreDetailRepository;
import com.agh.polymorphia_backend.service.mapper.HallOfFameMapper;
import com.agh.polymorphia_backend.service.student.AnimalService;
import com.agh.polymorphia_backend.service.user.UserService;
import com.agh.polymorphia_backend.service.validation.AccessAuthorizer;
import com.agh.polymorphia_backend.util.NumberFormatter;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

import static com.agh.polymorphia_backend.model.hall_of_fame.HallOfFameEntry.*;

@Service
@AllArgsConstructor
public class HallOfFameService {
    public static final String STUDENT_HOF_NOT_FOUND = "Brak wyników studenta.";
    private static final Set<String> INVERT_POSITION_FOR = Set.of(
            FIELD_TOTAL_XP_SUM,
            FIELD_TOTAL_BONUS_SUM
    );
    private final StudentScoreDetailRepository scoreDetailRepository;
    private final HallOfFameRepository hallOfFameRepository;
    private final EventSectionRepository eventSectionRepository;
    private final HallOfFameMapper hallOfFameMapper;
    private final AccessAuthorizer accessAuthorizer;
    private final AnimalService animalService;
    private final UserService userService;
    private final HallOfFameSortSpecResolver sortSpecResolver;

    private static Sort.Direction opposite(Sort.Direction direction) {
        return direction.isAscending() ? Sort.Direction.DESC : Sort.Direction.ASC;
    }

    public HallOfFameEntry getStudentHallOfFame(Long studentId, Long courseId) {
        Animal animal = animalService.getAnimal(studentId, courseId);
        return hallOfFameRepository.findByAnimalId(animal.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, STUDENT_HOF_NOT_FOUND));
    }

    public List<StudentScoreDetail> getStudentScoreDetails(Long animalId) {
        return scoreDetailRepository.findByAnimalId(animalId);
    }

    public StudentScoreDetail getStudentEventSectionScoreDetails(Long animalId, Long eventSectionId) {
        return scoreDetailRepository.findByAnimalIdAndEventSectionId(animalId, eventSectionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, STUDENT_HOF_NOT_FOUND));
    }

    public HallOfFameResponseDto getHallOfFame(HallOfFameRequestDto requestDto) {
        accessAuthorizer.authorizeCurrentUserCourseAccess(requestDto.courseId());

        HallOfFameSortSpec sortSpec = sortSpecResolver.resolve(requestDto);
        Page<HallOfFameEntry> hallOfFameEntryPage = switch (sortSpec) {
            case OverviewFieldSort overviewFieldSort ->
                    getSortedByOverviewFields(requestDto, overviewFieldSort.field());
            case EventSectionSort eventSectionSort -> getSortedByEventSection(requestDto);
        };
        Page<HallOfFameRecordDto> hallOfFameRecordDtoPage = hallOfFamePageToRecordDto(hallOfFameEntryPage);

        UserType currentUserRole = userService.getCurrentUserRole();
        if (UserType.STUDENT.equals(currentUserRole)) {
            Long animalId = animalService.getAnimalIdForCurrentUser(requestDto.courseId());

            if (hallOfFameEntryPage.stream().anyMatch(entry -> entry.getAnimalId().equals(animalId))) {
                return new HallOfFameResponseDto(hallOfFameRecordDtoPage, hallOfFameRecordDtoPage.getNumber());
            } else {
                List<Long> animalIds = getAllAnimalIds(requestDto, sortSpec);
                int position = animalIds.indexOf(animalId);
                int currentUserPage = position < 0 ? -1 : position / requestDto.size();
                return new HallOfFameResponseDto(hallOfFameRecordDtoPage, currentUserPage);
            }
        } else {
            return new HallOfFameResponseDto(hallOfFameRecordDtoPage, -1);
        }

    }

    public Page<HallOfFameEntry> getSortedByOverviewFields(HallOfFameRequestDto requestDto, String sortBy) {
        Pageable pageable = PageRequest.of(requestDto.page(), requestDto.size(), getOverviewSort(requestDto, sortBy));
        return hallOfFameRepository.findHofPageFromOverviewField(requestDto, pageable);
    }

    private Sort getOverviewSort(HallOfFameRequestDto requestDto, String sortBy) {
        Sort.Direction sortByDirection = requestDto.sortOrder().getDirection();
        Sort.Direction positionDirection = INVERT_POSITION_FOR.contains(sortBy) ? opposite(sortByDirection) : sortByDirection;
        return Sort.by(sortByDirection, sortBy).and(Sort.by(positionDirection, FIELD_POSITION));
    }

    public Page<HallOfFameEntry> getSortedByEventSection(HallOfFameRequestDto requestDto) {
        Pageable pageable = PageRequest.of(requestDto.page(), requestDto.size(), getEventSectionSort(requestDto));
        return hallOfFameRepository.findHofPageFromEventSection(requestDto, pageable);
    }

    private Sort getEventSectionSort(HallOfFameRequestDto requestDto) {
        Sort.Direction direction = requestDto.sortOrder().getDirection();
        return JpaSort.unsafe(direction, "ssd.rawXp").and(Sort.by(opposite(direction), FIELD_POSITION));
    }

    private Page<HallOfFameRecordDto> hallOfFamePageToRecordDto(Page<HallOfFameEntry> hallOfFamePage) {
        List<Long> animalIds = hallOfFamePage.getContent().stream()
                .map(HallOfFameEntry::getAnimalId)
                .toList();

        Map<Long, Map<String, String>> detailsByAnimalId = groupScoreDetails(animalIds);

        return hallOfFamePage.map(hallOfFame -> {
            Map<String, String> xpDetails = detailsByAnimalId.get(hallOfFame.getAnimalId());
            updateXpDetails(xpDetails, hallOfFame);
            return hallOfFameMapper.hallOfFameToRecordDto(hallOfFame, xpDetails);
        });
    }

    public Map<Long, Map<String, String>> groupScoreDetails(List<Long> animalIds) {
        List<StudentScoreDetail> detailsList = scoreDetailRepository.findByAnimalIdIn(animalIds);

        if (detailsList.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Brak wyników w Hall of Fame.");
        }

        sortByEventSectionOrderIndex(detailsList);
        Map<Long, Map<String, String>> result = new HashMap<>();

        for (StudentScoreDetail details : detailsList) {
            result.computeIfAbsent(details.getAnimalId(), id -> new LinkedHashMap<>())
                    .put(details.getEventSectionName(), NumberFormatter.formatToString(details.getRawXp()));
        }

        return result;
    }

    private void sortByEventSectionOrderIndex(List<StudentScoreDetail> detailsList) {
        Set<Long> eventSectionIds = detailsList.stream()
                .map(StudentScoreDetail::getEventSectionId).collect(Collectors.toSet());

        Map<Long, EventSection> eventSectionMap = eventSectionRepository.findByIdIn(eventSectionIds)
                .stream()
                .collect(Collectors.toMap(EventSection::getId, e -> e));

        detailsList.sort(Comparator.comparingLong(d ->
                eventSectionMap.get(d.getEventSectionId()).getOrderIndex()
        ));
    }

    public void updateXpDetails(Map<String, String> xpDetails, HallOfFameEntry hallOfFameEntry) {
        xpDetails.computeIfAbsent(OverviewField.BONUS.getKey(),
                k -> NumberFormatter.formatToString(hallOfFameEntry.getTotalBonusSum()));
        xpDetails.computeIfAbsent(OverviewField.TOTAL.getKey(),
                k -> NumberFormatter.formatToString(hallOfFameEntry.getTotalXpSum()));
    }

    private List<Long> getAllAnimalIds(HallOfFameRequestDto requestDto, HallOfFameSortSpec sortSpec) {
        return switch (sortSpec) {
            case OverviewFieldSort overviewFieldSort -> hallOfFameRepository.findAllAnimalIdsForOverviewField(
                    requestDto,
                    getOverviewSort(requestDto, overviewFieldSort.field())
            );
            case EventSectionSort eventSectionSort -> hallOfFameRepository.findAllAnimalIdsForEventSection(
                    requestDto,
                    getEventSectionSort(requestDto)
            );
        };
    }

    public List<HallOfFameRecordDto> getPodium(Long courseId) {
        accessAuthorizer.authorizeCurrentUserCourseAccess(courseId);

        HallOfFameRequestDto requestDto = new HallOfFameRequestDto(
                courseId,
                0,
                3,
                "",
                SearchBy.ANIMAL_NAME,
                OverviewField.TOTAL.getDbField(),
                SortOrder.DESC,
                Collections.emptyList()
        );
        return hallOfFamePageToRecordDto(getSortedByOverviewFields(requestDto, requestDto.sortBy())).getContent();
    }
}