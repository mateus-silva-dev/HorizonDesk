package io.github.mateussilvadev.horizondesk.model.enums;

import lombok.Getter;

@Getter
public enum PriorityTicket {
    LOW(1),
    MEDIUM(2),
    HIGH(3),
    CRITICAL(4);

    private final int weight;

    PriorityTicket(int weight) {
        this.weight = weight;
    }
}
