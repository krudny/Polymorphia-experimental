package com.agh.polymorphia_backend.service.knowledge_base;


import com.agh.polymorphia_backend.dto.response.knowledge_base.KnowledgeBaseResponseDto;
import com.agh.polymorphia_backend.repository.course.EvolutionStagesRepository;
import com.agh.polymorphia_backend.repository.reward.ChestRepository;
import com.agh.polymorphia_backend.repository.reward.ItemRepository;
import com.agh.polymorphia_backend.service.course.CourseService;
import com.agh.polymorphia_backend.service.mapper.KnowledgeBaseMapper;
import com.agh.polymorphia_backend.service.validation.AccessAuthorizer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

@Service
@AllArgsConstructor
public class KnowledgeBaseService {

    private final EvolutionStagesRepository evolutionStagesRepository;
    private final ChestRepository chestRepository;
    private final ItemRepository itemRepository;
    private final KnowledgeBaseMapper knowledgeBaseMapper;
    private final AccessAuthorizer accessAuthorizer;
    private final CourseService courseService;

    public List<KnowledgeBaseResponseDto> getEvolutionStages(Long courseId) {
        return getKnowledgeBaseResponseDto(
                courseId,
                evolutionStagesRepository::findAllByCourseId,
                knowledgeBaseMapper::evolutionStageToResponseDto
        );
    }

    public List<KnowledgeBaseResponseDto> getChests(Long courseId) {
        return getKnowledgeBaseResponseDto(
                courseId,
                chestRepository::findAllByCourseId,
                knowledgeBaseMapper::chestToResponseDto
        );
    }

    public List<KnowledgeBaseResponseDto> getItems(Long courseId) {
        return getKnowledgeBaseResponseDto(
                courseId,
                itemRepository::findAllByCourseId,
                knowledgeBaseMapper::itemToResponseDto
        );
    }

    private <T> List<KnowledgeBaseResponseDto> getKnowledgeBaseResponseDto(
            Long courseId,
            Function<Long, List<T>> repositoryFetcher,
            Function<T, KnowledgeBaseResponseDto> mapper
    ) {
        accessAuthorizer.authorizeCurrentUserCourseAccess(courseId);

        return repositoryFetcher.apply(courseId)
                .stream()
                .map(mapper)
                .sorted(Comparator.comparing(KnowledgeBaseResponseDto::getOrderIndex))
                .toList();
    }

}
