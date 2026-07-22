package io.github.mateussilvadev.horizondesk.repository;

import io.github.mateussilvadev.horizondesk.model.domain.TicketHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TicketHistoryRepository extends JpaRepository<TicketHistory, Long> {
    List<TicketHistory> findAllByTicketUuidOrderByCreatedAtAsc(UUID ticketUuid);
}
