package io.github.mateussilvadev.horizondesk.controller;

import io.github.docflowlib.docflow.annotations.ApiDocController;
import io.github.docflowlib.docflow.annotations.ApiDocGet;
import io.github.docflowlib.docflow.annotations.ApiDocPatch;
import io.github.docflowlib.docflow.annotations.ApiDocPost;
import io.github.mateussilvadev.horizondesk.dto.request.TicketRequestDTOs;
import io.github.mateussilvadev.horizondesk.dto.response.TicketResponseDTOs;
import io.github.mateussilvadev.horizondesk.mapper.TicketMapper;
import io.github.mateussilvadev.horizondesk.model.domain.Ticket;
import io.github.mateussilvadev.horizondesk.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tickets")
@ApiDocController
public class TicketController {

    private final TicketService service;

    public TicketController(TicketService service) {
        this.service = service;
    }

    @PostMapping
    @ApiDocPost
    public ResponseEntity<TicketResponseDTOs.TicketResponse> createTicket(@Valid @RequestBody TicketRequestDTOs.TicketCreate dto) {
        Ticket ticketSaved = service.create(dto);
        TicketResponseDTOs.TicketResponse ticketResponse = TicketMapper.toResponse(ticketSaved);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{uuid}")
                .buildAndExpand(ticketResponse.uuid())
                .toUri();
        return ResponseEntity.created(uri).body(ticketResponse);
    }

    @GetMapping("/{uuid}")
    @ApiDocGet
    public ResponseEntity<TicketResponseDTOs.TicketResponse> getByUUID(@PathVariable UUID uuid) {
        Ticket ticket = service.findByUuid(uuid);
        return ResponseEntity.ok(TicketMapper.toResponse(ticket));
    }

    @PatchMapping("/{uuid}")
    @ApiDocPatch
    public ResponseEntity<TicketResponseDTOs.TicketResponse> updateTicket(@PathVariable UUID uuid, @Valid @RequestBody TicketRequestDTOs.TicketUpdate dto) {
        Ticket ticket = service.updateTicket(uuid, dto);
        return ResponseEntity.ok(TicketMapper.toResponse(ticket));
    }

    @PatchMapping("/{uuid}/priority")
    @ApiDocPatch
    public ResponseEntity<Void> changePriority(@PathVariable UUID uuid, @Valid @RequestBody TicketRequestDTOs.TicketChangePriority dto) {
        service.changePriority(uuid, dto.priorityTicket());
        return ResponseEntity.noContent().build();
    }
}
