package com.agh.polymorphia_backend.repository.reward;

import com.agh.polymorphia_backend.model.reward.Reward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RewardRepository extends JpaRepository<Reward, Long> {
    @Query("""
            select r
            from Reward r
            where r.key=:key and r.course.id=:courseId
            """)
    Reward findByKeyAndCourseId(String key, Long courseId);
}