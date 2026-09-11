package com.agh.polymorphia_backend.service.task.async_submission;

import com.agh.polymorphia_backend.model.criterion.CriterionGrade;
import com.agh.polymorphia_backend.model.gradable_event.subtypes.task.Task;
import com.agh.polymorphia_backend.model.gradable_event.subtypes.task.TaskSubmission;
import com.agh.polymorphia_backend.model.grade.Grade;
import com.agh.polymorphia_backend.model.user.student.Animal;
import com.agh.polymorphia_backend.repository.task.TaskSubmissionRepository;
import com.agh.polymorphia_backend.service.criteria.CriterionGradeService;
import com.agh.polymorphia_backend.service.grade.GradeService;
import com.agh.polymorphia_backend.service.reward.BonusXpCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TaskSubmissionGradingService {

    private final TaskSubmissionRepository taskSubmissionRepository;
    private final GradeService gradeService;
    private final CriterionGradeService criterionGradeService;
    private final BonusXpCalculator bonusXpCalculator;

    @Transactional
    public void applyGrade(Long taskSubmissionId) {
        TaskSubmission taskSubmission = taskSubmissionRepository.findById(taskSubmissionId).orElse(null);

        if (taskSubmission == null) {
            return;
        }

        Animal animal = taskSubmission.getAnimal();
        Task task = taskSubmission.getTask();
        BigDecimal scorePercentage = taskSubmission.getScore() != null ? taskSubmission.getScore() : BigDecimal.ZERO;

        assignGradeToAnimal(animal, task, scorePercentage);
    }

    private void assignGradeToAnimal(Animal animal, Task task, BigDecimal scorePercentage) {
        BigDecimal scoreRatio = scorePercentage.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);

        Optional<Grade> existingGradeOptional = gradeService.getGradeByAnimalIdAndGradableEventId(animal.getId(), task.getId());

        if (existingGradeOptional.isPresent()) {
            Grade existingGrade = existingGradeOptional.get();
            BigDecimal existingTotalXp = existingGrade.getCriteriaGrades().stream()
                    .map(CriterionGrade::getXp)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal newTotalXp = task.getCriteria().stream()
                    .map(criterion -> criterion.getMaxXp().multiply(scoreRatio).setScale(1, RoundingMode.HALF_UP))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (newTotalXp.compareTo(existingTotalXp) <= 0) {
                return;
            }
        }

        Grade grade = gradeService.getOrCreateGrade(animal, task, "Rozwiązanie automatyczne");
        List<CriterionGrade> criteriaGrades = new ArrayList<>();

        task.getCriteria().forEach(criterion -> {
            BigDecimal gainedXp = criterion.getMaxXp().multiply(scoreRatio).setScale(1, RoundingMode.HALF_UP);
            CriterionGrade criterionGrade = criterionGradeService.fetchOrCreateCriterionGrade(
                    criterion.getId(),
                    gainedXp,
                    grade
            );
            criteriaGrades.add(criterionGrade);
        });

        grade.getCriteriaGrades().clear();
        grade.getCriteriaGrades().addAll(criteriaGrades);
        gradeService.saveGrade(grade);
        criterionGradeService.saveAll(criteriaGrades);

        bonusXpCalculator.updateAnimalFlatBonusXp(animal.getId());
        bonusXpCalculator.updateAnimalPercentageBonusXp(animal.getId());
    }
}
