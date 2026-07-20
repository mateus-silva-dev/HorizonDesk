package io.github.mateussilvadev.horizondesk.model.enums;

import io.github.mateussilvadev.horizondesk.exception.BusinessException;
import io.github.mateussilvadev.horizondesk.exception.Code;
import lombok.Getter;

import java.util.EnumSet;
import java.util.Map;

@Getter
public enum StatusTicket {
    OPEN("OPEN"),
    IN_PROGRESS("IN_PROGRESS"),
    RESOLVED("RESOLVED"),
    CLOSED("CLOSED");

    private final String value;

    StatusTicket(String value) {
        this.value = value;
    }

    private static final Map<StatusTicket, EnumSet<StatusTicket>> VALID_TRANSITIONS;
    static {
        VALID_TRANSITIONS = Map.of(
                OPEN, EnumSet.of(IN_PROGRESS),
                IN_PROGRESS, EnumSet.of(RESOLVED),
                RESOLVED, EnumSet.of(IN_PROGRESS, CLOSED),
                CLOSED, EnumSet.noneOf(StatusTicket.class)
        );
    }

    public void transitionTo(StatusTicket nextStatus) {
        if (nextStatus == null)
            throw new BusinessException(Code.BUSINESS_RULE, "ticket.error.invalid_status_transition");

        EnumSet<StatusTicket> allowedTargets = VALID_TRANSITIONS.get(this);

        if (allowedTargets == null || !allowedTargets.contains(nextStatus)) {
            throw new BusinessException(Code.BUSINESS_RULE, "ticket.error.invalid_status_transition");
        }
    }
}
