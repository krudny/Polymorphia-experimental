package com.agh.polymorphia_backend.service.criteria;

import com.agh.polymorphia_backend.dto.response.criteria.CriterionResponseDto;
import com.agh.polymorphia_backend.model.gradable_event.GradableEvent;
import com.agh.polymorphia_backend.service.gradable_event.GradableEventService;
import com.agh.polymorphia_backend.service.mapper.CriterionMapper;
import com.agh.polymorphia_backend.service.validation.AccessAuthorizer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class CriteriaService {
    private final GradableEventService gradableEventService;
    private final AccessAuthorizer accessAuthorizer;
    private final CriterionMapper criterionMapper;


    public List<CriterionResponseDto> getCriteria(Long gradableEventId) {
        GradableEvent gradableEvent = gradableEventService.getGradableEventById(gradableEventId);
        accessAuthorizer.authorizeCurrentUserCourseAccess(gradableEventService.getCourseIdByGradableEventId(gradableEventId));

        return gradableEvent.getCriteria().stream()
                .map(criterionMapper::toCriterionResponseDto)
                .toList();
    }

}
