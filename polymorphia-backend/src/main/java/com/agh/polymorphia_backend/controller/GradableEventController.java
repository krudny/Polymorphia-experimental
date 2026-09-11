package com.agh.polymorphia_backend.controller;

import com.agh.polymorphia_backend.dto.response.event.BaseGradableEventResponseDto;
import com.agh.polymorphia_backend.dto.response.reward.points_summary.PointsSummaryResponseDto;
import com.agh.polymorphia_backend.model.gradable_event.GradableEventScope;
import com.agh.polymorphia_backend.model.gradable_event.GradableEventSortBy;
import com.agh.polymorphia_backend.service.gradable_event.GradableEventService;
import com.agh.polymorphia_backend.service.gradable_event.PointsSummaryService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/gradable-events")
public class GradableEventController {
    private final GradableEventService gradableEventService;
    private final PointsSummaryService pointsSummaryService;

    @GetMapping("/{gradableEventId}")
    @PreAuthorize("hasAnyAuthority('STUDENT', 'INSTRUCTOR', 'COORDINATOR')")
    public ResponseEntity<BaseGradableEventResponseDto> getGradableEvent(@PathVariable Long gradableEventId) {
        return ResponseEntity.ok(gradableEventService.getGradableEventResponseDto(gradableEventId));
    }

    @GetMapping()
    @PreAuthorize("hasAnyAuthority('STUDENT', 'INSTRUCTOR', 'COORDINATOR')")
    public ResponseEntity<List<BaseGradableEventResponseDto>> getGradableEvents(@RequestParam Long eventSectionId) {
        return ResponseEntity.ok(gradableEventService.getGradableEvents(eventSectionId, GradableEventScope.EVENT_SECTION, GradableEventSortBy.ORDER_INDEX, false));
    }

    @GetMapping("/points-summary")
    @PreAuthorize("hasAnyAuthority('STUDENT', 'INSTRUCTOR', 'COORDINATOR')")
    public ResponseEntity<PointsSummaryResponseDto> getPointsSummary(@RequestParam Long eventSectionId) {
        return ResponseEntity.ok(pointsSummaryService.getPointsSummary(eventSectionId));
    }
}
