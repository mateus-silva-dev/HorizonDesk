package io.github.mateussilvadev.horizondesk.repository;

import io.github.mateussilvadev.horizondesk.model.domain.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, UUID>, JpaSpecificationExecutor<Ticket> {
    Optional<Ticket> findByUuid(UUID uuid);
    boolean existsByUuid(UUID ticketUuid);
}
