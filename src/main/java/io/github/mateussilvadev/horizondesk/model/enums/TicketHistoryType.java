package io.github.mateussilvadev.horizondesk.model.enums;

import lombok.Getter;

@Getter
public enum TicketHistoryType {
    OPENED("ticket.history.opened"),
    UPDATED("ticket.history.updated"),
    PRIORITY_CHANGED("ticket.history.priority-changed"),
    TECHNICIAN_ASSIGNED("ticket.history.technician-assigned"),
    RESOLVED("ticket.history.resolved"),
    REOPENED("ticket.history.reopened"),
    CLOSED("ticket.history.closed");

    private final String messageKey;

    TicketHistoryType(String messageKey) {
        this.messageKey = messageKey;
    }
}