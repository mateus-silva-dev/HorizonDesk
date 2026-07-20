package io.github.mateussilvadev.horizondesk.repository;

import io.github.mateussilvadev.horizondesk.model.domain.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {
    Optional<Ticket> findByUuid(UUID uuid);
}
