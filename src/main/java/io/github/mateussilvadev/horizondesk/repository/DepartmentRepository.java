package io.github.mateussilvadev.horizondesk.repository;

import io.github.mateussilvadev.horizondesk.model.domain.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DepartmentRepository extends JpaRepository<Department, UUID> {
    Optional<Department> findByUuid(UUID uuid);
    boolean existsByName(String name);
}
