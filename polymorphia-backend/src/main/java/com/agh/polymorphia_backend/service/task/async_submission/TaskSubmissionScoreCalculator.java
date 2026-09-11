package com.agh.polymorphia_backend.service.task.async_submission;

import com.agh.polymorphia_backend.model.gradable_event.subtypes.task.TaskGradingStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class TaskSubmissionScoreCalculator {

    public BigDecimal calculateScorePercentage(
            TaskGradingStrategy taskGradingStrategy,
            int passedCount,
            int totalCount,
            BigDecimal passedWeight,
            BigDecimal totalWeight
    ) {
        if (totalCount == 0) {
            return BigDecimal.ZERO;
        }

        if (taskGradingStrategy == TaskGradingStrategy.ALL_OR_NONE) {
            return passedCount == totalCount ? BigDecimal.valueOf(100) : BigDecimal.ZERO;
        }

        if (totalWeight.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return passedWeight.multiply(BigDecimal.valueOf(100)).divide(totalWeight, 2, RoundingMode.HALF_UP);
    }
}
