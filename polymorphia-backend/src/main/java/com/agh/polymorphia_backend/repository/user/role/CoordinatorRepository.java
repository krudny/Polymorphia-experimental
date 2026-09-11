package com.agh.polymorphia_backend.repository.user.role;

import com.agh.polymorphia_backend.model.user.coordinator.Coordinator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CoordinatorRepository extends JpaRepository<Coordinator, Long> {
}
