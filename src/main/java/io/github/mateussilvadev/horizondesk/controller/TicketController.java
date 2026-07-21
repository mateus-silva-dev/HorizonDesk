package io.github.mateussilvadev.horizondesk.controller;

import io.github.docflowlib.docflow.annotations.ApiDocController;
import io.github.docflowlib.docflow.annotations.ApiDocGet;
import io.github.docflowlib.docflow.annotations.ApiDocPatch;
import io.github.docflowlib.docflow.annotations.ApiDocPost;
import io.github.mateussilvadev.horizondesk.dto.request.TicketFilter;
import io.github.mateussilvadev.horizondesk.dto.request.TicketRequestDTOs;
import io.github.mateussilvadev.horizondesk.dto.response.PageResponse;
import io.github.mateussilvadev.horizondesk.dto.response.TicketHistoryResponse;
import io.github.mateussilvadev.horizondesk.dto.response.TicketResponseDTOs;
import io.github.mateussilvadev.horizondesk.mapper.TicketHistoryMapper;
import io.github.mateussilvadev.horizondesk.mapper.TicketMapper;
import io.github.mateussilvadev.horizondesk.model.domain.Ticket;
import io.github.mateussilvadev.horizondesk.model.domain.TicketHistory;
import io.github.mateussilvadev.horizondesk.service.TicketHistoryService;
import io.github.mateussilvadev.horizondesk.service.TicketService;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tickets")
@ApiDocController
public class TicketController {

    private final TicketService service;
    private final TicketHistoryService historyService;
    private final TicketHistoryMapper mapper;

    public TicketController(TicketService service, TicketHistoryService historyService, TicketHistoryMapper mapper) {
        this.service = service;
        this.historyService = historyService;
        this.mapper = mapper;
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

    @PatchMapping("/{uuid}/assign")
    @ApiDocPatch
    public ResponseEntity<Void> assignTechnician(@PathVariable UUID uuid, @Valid @RequestBody TicketRequestDTOs.TicketAssignTechnician dto) {
        service.assignTechnician(uuid, dto.technicianUuid());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{uuid}/resolve")
    @ApiDocPatch
    public ResponseEntity<Void> resolveTicket(@PathVariable UUID uuid) {
        service.resolveTicket(uuid);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{uuid}/reopen")
    @ApiDocPatch
    public ResponseEntity<Void> reopenTicket(@PathVariable UUID uuid) {
        service.reopenTicket(uuid);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{uuid}/close")
    @ApiDocPatch
    public ResponseEntity<Void> closeTicket(@PathVariable UUID uuid) {
        service.closeTicket(uuid);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @ApiDocGet
    public ResponseEntity<PageResponse<TicketResponseDTOs.TicketResponse>> search(@ParameterObject TicketFilter filter,
                                                                                  @ParameterObject @PageableDefault(
                                                                                          size = 20,
                                                                                          sort = {"priorityWeight", "createdAt"},
                                                                                          direction = Sort.Direction.DESC) Pageable pageable) {

        Page<Ticket> page = service.findAllPaginated(filter, pageable);
        return ResponseEntity.ok(TicketMapper.toTicketResponse(page));
    }

    @GetMapping("/{uuid}/history")
    public ResponseEntity<List<TicketHistoryResponse>> findHistory(@PathVariable UUID uuid) {
        List<TicketHistory> histories = historyService.findByTicketUuid(uuid);
        return ResponseEntity.ok(mapper.toResponseList(histories));
    }
}
