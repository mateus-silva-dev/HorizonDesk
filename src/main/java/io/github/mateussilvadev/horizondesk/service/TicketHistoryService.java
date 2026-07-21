package io.github.mateussilvadev.horizondesk.service;

import io.github.mateussilvadev.horizondesk.exception.BusinessException;
import io.github.mateussilvadev.horizondesk.exception.Code;
import io.github.mateussilvadev.horizondesk.model.domain.TicketHistory;
import io.github.mateussilvadev.horizondesk.repository.TicketHistoryRepository;
import io.github.mateussilvadev.horizondesk.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class TicketHistoryService {

    private final TicketRepository ticketRepository;
    private final TicketHistoryRepository historyRepository;

    public TicketHistoryService(TicketRepository ticketRepository, TicketHistoryRepository historyRepository) {
        this.ticketRepository = ticketRepository;
        this.historyRepository = historyRepository;
    }

    @Transactional(readOnly = true)
    public List<TicketHistory> findByTicketUuid(UUID ticketUuid) {
        if (!ticketRepository.existsByUuid(ticketUuid)) {
            throw new BusinessException(Code.ENTITY_NOT_FOUND, "ticket.not_found");
        }

        return historyRepository.findAllByTicketUuidOrderByCreatedAtAsc(ticketUuid);
    }
}
