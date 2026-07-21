package io.github.mateussilvadev.horizondesk.dto.request;

import io.github.mateussilvadev.horizondesk.model.enums.PriorityTicket;
import io.github.mateussilvadev.horizondesk.model.enums.StatusTicket;

import java.util.UUID;

public record TicketFilter(
        StatusTicket status,
        PriorityTicket priority,
        UUID departmentUuid,
        UUID requesterUuid,
        UUID technicianUuid) { }
