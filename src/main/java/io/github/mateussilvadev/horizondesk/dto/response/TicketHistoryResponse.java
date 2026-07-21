package io.github.mateussilvadev.horizondesk.dto.response;

import io.github.mateussilvadev.horizondesk.model.enums.TicketHistoryType;

import java.time.LocalDateTime;
import java.util.UUID;

public record TicketHistoryResponse(
        UUID uuid,
        //TicketHistoryType type,
        String message,
        //UUID actorUuid,
        String actorName ) { }
        //LocalDateTime createdAt) { }
