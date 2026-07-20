package io.github.mateussilvadev.horizondesk.dto.request;

import io.github.mateussilvadev.horizondesk.model.enums.PriorityTicket;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public final class TicketRequestDTOs {

    private TicketRequestDTOs() { }

    public record TicketCreate(
            @NotBlank(message = "Description is required.")
            @NotNull(message = "Description is required.")
            @Size(min = 10, max = 200, message = "Description must be between 10 and 200 characters.")
            String description,

            @NotNull(message = "Priority Ticket is required.")
            PriorityTicket priorityTicket,

            @NotNull(message = "UUID of user is required.")
            UUID requesterUuid) { }

    public record TicketUpdate(
            @NotBlank(message = "Description is required.")
            @NotNull(message = "Description is required.")
            @Size(min = 10, max = 200, message = "Description must be between 10 and 200 characters.")
            String description) { }
}
