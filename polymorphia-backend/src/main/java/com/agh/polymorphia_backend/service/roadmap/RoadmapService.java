package com.agh.polymorphia_backend.service.roadmap;

import com.agh.polymorphia_backend.dto.response.event.BaseGradableEventResponseDto;
import com.agh.polymorphia_backend.model.gradable_event.GradableEventScope;
import com.agh.polymorphia_backend.model.gradable_event.GradableEventSortBy;
import com.agh.polymorphia_backend.service.gradable_event.GradableEventService;
import com.agh.polymorphia_backend.service.validation.AccessAuthorizer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class RoadmapService {
    private final AccessAuthorizer accessAuthorizer;
    private final GradableEventService gradableEventService;

    public List<BaseGradableEventResponseDto> getRoadmap(Long courseId) {
        accessAuthorizer.authorizeCurrentUserCourseAccess(courseId);
        return gradableEventService.getGradableEvents(courseId, GradableEventScope.COURSE, GradableEventSortBy.ROADMAP_ORDER_INDEX, true);
    }
}
