package io.github.mateussilvadev.horizondesk.dto.response;

import io.github.mateussilvadev.horizondesk.model.enums.PriorityTicket;
import io.github.mateussilvadev.horizondesk.model.enums.StatusTicket;

import java.time.LocalDateTime;
import java.util.UUID;

public final class TicketResponseDTOs {

    private TicketResponseDTOs() { }

    public record TicketResponse(
         UUID uuid,
         String title,
         String description,
         PriorityTicket priority,
         StatusTicket status,
         String user,
         String department,
         LocalDateTime createdAt) { }
}
