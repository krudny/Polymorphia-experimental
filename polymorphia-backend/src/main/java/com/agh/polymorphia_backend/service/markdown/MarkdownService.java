package com.agh.polymorphia_backend.service.markdown;

import com.agh.polymorphia_backend.dto.response.markdown.MarkdownResponseDto;
import com.agh.polymorphia_backend.dto.response.markdown.SourceUrlMarkdownResponseDto;
import com.agh.polymorphia_backend.repository.gradable_event.GradableEventRepository;
import com.agh.polymorphia_backend.service.markdown.strategy.MarkdownCourseStrategy;
import com.agh.polymorphia_backend.service.markdown.strategy.MarkdownGradableEventStrategy;
import com.agh.polymorphia_backend.service.markdown.strategy.MarkdownStrategy;
import com.agh.polymorphia_backend.service.validation.AccessAuthorizer;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static com.agh.polymorphia_backend.service.course.CourseService.COURSE_NOT_FOUND;

@Service
@AllArgsConstructor
public class MarkdownService {
    private final MarkdownCourseStrategy markdownCourseStrategy;
    private final GradableEventRepository gradableEventRepository;
    private final MarkdownGradableEventStrategy markdownGradableEventStrategy;
    private final AccessAuthorizer accessAuthorizer;

    public MarkdownResponseDto getMarkdown(MarkdownType type, Long resourceId) {
        accessAuthorizer.authorizeCurrentUserCourseAccess(getCourseIdForResourceId(resourceId));

        return getStrategyForType(type).getMarkdown(resourceId);
    }

    public SourceUrlMarkdownResponseDto getMarkdownSourceUrl(MarkdownType type, Long resourceId) {
        accessAuthorizer.authorizeCurrentUserCourseAccess(getCourseIdForResourceId(resourceId));

        return getStrategyForType(type).getMarkdownSourceUrl(resourceId);
    }

    public void setMarkdown(MarkdownType type, Long resourceId, String markdown) {
        accessAuthorizer.authorizeCurrentUserCourseAccess(getCourseIdForResourceId(resourceId));

        getStrategyForType(type).setMarkdown(resourceId, markdown);
    }

    public void resetMarkdown(MarkdownType type, Long resourceId) {
        accessAuthorizer.authorizeCurrentUserCourseAccess(getCourseIdForResourceId(resourceId));

        getStrategyForType(type).resetMarkdown(resourceId);
    }

    private MarkdownStrategy getStrategyForType(MarkdownType type) {
        return switch (type) {
            case GRADABLE_EVENT -> markdownGradableEventStrategy;
            case COURSE_RULES -> markdownCourseStrategy;
        };
    }

    private Long getCourseIdForResourceId(Long resourceId) {
        return gradableEventRepository.getCourseIdByGradableEventId(resourceId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, COURSE_NOT_FOUND));
    }
}
