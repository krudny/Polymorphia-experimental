package com.agh.polymorphia_backend.repository.criterion;

import com.agh.polymorphia_backend.model.criterion.CriterionReward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CriterionRewardRepository extends JpaRepository<CriterionReward, Long> {
    void deleteByCriterionId(Long criterionId);
}
