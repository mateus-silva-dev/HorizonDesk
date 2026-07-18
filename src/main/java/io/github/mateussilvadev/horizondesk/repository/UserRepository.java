package io.github.mateussilvadev.horizondesk.repository;

import io.github.mateussilvadev.horizondesk.dto.response.UserResponseDTOs;
import io.github.mateussilvadev.horizondesk.model.domain.User;
import io.github.mateussilvadev.horizondesk.model.enums.Role;
import io.github.mateussilvadev.horizondesk.model.enums.StatusUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByEmail(String email);
    Optional<User> findByUuid(UUID uuid);
    long countByRole(Role role);
    Page<User> findAllByRoleAndStatus(Role role, StatusUser status, Pageable pageable);
}
