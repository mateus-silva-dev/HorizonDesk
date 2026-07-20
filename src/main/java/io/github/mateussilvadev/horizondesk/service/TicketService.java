package io.github.mateussilvadev.horizondesk.service;

import io.github.mateussilvadev.horizondesk.dto.request.TicketRequestDTOs;
import io.github.mateussilvadev.horizondesk.exception.BusinessException;
import io.github.mateussilvadev.horizondesk.exception.Code;
import io.github.mateussilvadev.horizondesk.exception.EntityNotFoundException;
import io.github.mateussilvadev.horizondesk.mapper.TicketMapper;
import io.github.mateussilvadev.horizondesk.model.domain.Ticket;
import io.github.mateussilvadev.horizondesk.model.domain.User;
import io.github.mateussilvadev.horizondesk.model.enums.PriorityTicket;
import io.github.mateussilvadev.horizondesk.repository.TicketRepository;
import io.github.mateussilvadev.horizondesk.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class TicketService {

    private final TicketRepository repository;
    private final UserRepository userRepository;

    public TicketService(TicketRepository repository, UserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Ticket create(TicketRequestDTOs.TicketCreate dto) {
        User user = userRepository.findByUuid(dto.requesterUuid())
                .orElseThrow(() -> new EntityNotFoundException("ticket_service.error.requester_not_found"));
        Ticket ticket = TicketMapper.toTicket(dto, user);
        Ticket savedTicket = repository.saveAndFlush(ticket);
        savedTicket.generateFinalTitle();
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
        return ticket;
    }

    @Transactional
    public void changePriority(UUID uuid, PriorityTicket newPriority) {
        Ticket ticket = findByUuid(uuid);
        ticket.changePriority(newPriority);
    }

    @Transactional
    public void assignTechnician(UUID ticketUuid, UUID technicianUuid) {
        Ticket ticket = findByUuid(ticketUuid);
        User technician = userRepository.findByUuid(technicianUuid)
                .orElseThrow(() -> new EntityNotFoundException("ticket_service.error.technician_not_found"));
        ticket.assignTechnician(technician);
    }

    @Transactional
    public void resolveTicket(UUID uuid) {
        Ticket ticket = findByUuid(uuid);
        ticket.resolve();
    }

    @Transactional
    public void closeTicket(UUID uuid) {
        Ticket ticket = findByUuid(uuid);
        ticket.close();
    }

}
