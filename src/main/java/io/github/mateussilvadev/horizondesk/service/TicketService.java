package io.github.mateussilvadev.horizondesk.service;

import io.github.mateussilvadev.horizondesk.dto.request.TicketFilter;
import io.github.mateussilvadev.horizondesk.dto.request.TicketRequestDTOs;
import io.github.mateussilvadev.horizondesk.exception.BusinessException;
import io.github.mateussilvadev.horizondesk.exception.Code;
import io.github.mateussilvadev.horizondesk.exception.EntityNotFoundException;
import io.github.mateussilvadev.horizondesk.mapper.TicketMapper;
import io.github.mateussilvadev.horizondesk.model.domain.Ticket;
import io.github.mateussilvadev.horizondesk.model.domain.TicketHistory;
import io.github.mateussilvadev.horizondesk.model.domain.User;
import io.github.mateussilvadev.horizondesk.model.enums.PriorityTicket;
import io.github.mateussilvadev.horizondesk.model.enums.StatusTicket;
import io.github.mateussilvadev.horizondesk.repository.TicketHistoryRepository;
import io.github.mateussilvadev.horizondesk.repository.TicketRepository;
import io.github.mateussilvadev.horizondesk.repository.TicketSpecifications;
import io.github.mateussilvadev.horizondesk.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class TicketService {

    private final TicketRepository repository;
    private final UserRepository userRepository;
    private final TicketHistoryRepository historyRepository;

    public TicketService(TicketRepository repository, UserRepository userRepository, TicketHistoryRepository historyRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.historyRepository = historyRepository;
    }

    @Transactional
    public Ticket create(TicketRequestDTOs.TicketCreate dto) {
        User user = userRepository.findByUuid(dto.requesterUuid())
                .orElseThrow(() -> new EntityNotFoundException("ticket_service.error.requester_not_found"));
        Ticket ticket = TicketMapper.toTicket(dto, user);
        Ticket savedTicket = repository.saveAndFlush(ticket);
        savedTicket.generateFinalTitle();

        TicketHistory history = TicketHistory.opened(ticket, user);
        historyRepository.save(history);

        return repository.save(savedTicket);
    }

    @Transactional(readOnly = true)
    public Ticket findByUuid(UUID uuid) {
        return repository.findByUuid(uuid)
                .orElseThrow(() -> new EntityNotFoundException("ticket_service.error.ticket_not_found"));
    }

    @Transactional
    public Ticket updateTicket(UUID uuid, TicketRequestDTOs.TicketUpdate dto) {
        Ticket ticket = findByUuid(uuid);
        if (dto.description() != null)
            ticket.changeDescription(dto.description());

        TicketHistory history = TicketHistory.update(ticket, ticket.getRequester());
        historyRepository.save(history);

        return ticket;
    }

    @Transactional
    public void changePriority(UUID uuid, PriorityTicket newPriority) {
        Ticket ticket = findByUuid(uuid);

        PriorityTicket oldPriority = ticket.getPriority();

        ticket.changePriority(newPriority);

        TicketHistory history = TicketHistory.changePriority(ticket, ticket.getRequester(), oldPriority, newPriority);
        historyRepository.save(history);
    }

    @Transactional
    public void assignTechnician(UUID ticketUuid, UUID technicianUuid) {
        Ticket ticket = findByUuid(ticketUuid);
        User technician = userRepository.findByUuid(technicianUuid)
                .orElseThrow(() -> new EntityNotFoundException("ticket_service.error.technician_not_found"));
        ticket.assignTechnician(technician);

        TicketHistory history = TicketHistory.assignTechnician(ticket, ticket.getRequester(), technician);
        historyRepository.save(history);
    }

    @Transactional
    public void resolveTicket(UUID uuid) {
        Ticket ticket = findByUuid(uuid);

        ticket.resolve();

        TicketHistory history = TicketHistory.resolve(ticket, ticket.getAssignedTechnician());
        historyRepository.save(history);
    }

    @Transactional
    public void reopenTicket(UUID uuid) {
        Ticket ticket = findByUuid(uuid);

        ticket.reopen();

        TicketHistory history = TicketHistory.reopen(ticket, ticket.getRequester());
        historyRepository.save(history);
    }

    @Transactional
    public void closeTicket(UUID uuid) {
        Ticket ticket = findByUuid(uuid);

        ticket.close();

        TicketHistory history = TicketHistory.close(ticket, ticket.getRequester());
        historyRepository.save(history);
    }

    @Transactional(readOnly = true)
    public Page<Ticket> findAllPaginated(TicketFilter filter, Pageable pageable) {
        Specification<Ticket> spec = TicketSpecifications.withFilter(filter);
        return repository.findAll(spec, pageable);
    }

}
