package io.github.mateussilvadev.horizondesk.model;

import io.github.mateussilvadev.horizondesk.builder.TicketBuilder;
import io.github.mateussilvadev.horizondesk.builder.UserBuilder;
import io.github.mateussilvadev.horizondesk.model.domain.Ticket;
import io.github.mateussilvadev.horizondesk.model.domain.TicketHistory;
import io.github.mateussilvadev.horizondesk.model.domain.User;
import io.github.mateussilvadev.horizondesk.model.enums.PriorityTicket;
import io.github.mateussilvadev.horizondesk.model.enums.TicketHistoryType;
import net.datafaker.Faker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Tickethistory test")
public class TicketHistoryTest {

    private static final Faker faker = new Faker(Locale.of("pt", "BR"));

    private final User technician = UserBuilder.anUser().build();
    private final User actor = UserBuilder.anUser().build();
    private final Ticket ticket = TicketBuilder.anTicket().build();

    @Test
    void shouldCreateOpenedHistory() {
        TicketHistory history = TicketHistory.opened(ticket, actor);

        assertEquals(TicketHistoryType.OPENED, history.getType());
        assertEquals(actor, history.getActor());
    }

    @Test
    void shouldCreateAssignTechnicianHistory() {
        TicketHistory history = TicketHistory.assignTechnician(ticket, actor, technician);

        assertEquals(TicketHistoryType.TECHNICIAN_ASSIGNED, history.getType());
        assertEquals(technician.getName(), history.getNewValue());
    }

    @Test
    void shouldCreateChangePriorityHistory() {
        TicketHistory history = TicketHistory.changePriority(ticket, actor, PriorityTicket.LOW, PriorityTicket.MEDIUM);

        assertEquals(TicketHistoryType.PRIORITY_CHANGED, history.getType());
        assertEquals(actor, history.getActor());
        assertEquals("LOW", history.getOldValue());
        assertEquals("MEDIUM", history.getNewValue());
    }

    @Test
    void shouldCreateUpdateHistory() {
        TicketHistory history = TicketHistory.update(ticket, actor);

        assertEquals(TicketHistoryType.UPDATED, history.getType());
        assertEquals(actor, history.getActor());
    }

    @Test
    void shouldCreateResolvedHistory() {
        TicketHistory history = TicketHistory.resolve(ticket, actor);

        assertEquals(TicketHistoryType.RESOLVED, history.getType());
        assertEquals(actor, history.getActor());
    }

    @Test
    void shouldCreateReopenHistory() {
        TicketHistory history = TicketHistory.reopen(ticket, actor);

        assertEquals(TicketHistoryType.REOPENED, history.getType());
        assertEquals(actor, history.getActor());
    }

    @Test
    void shouldCreateCloseHistory() {
        TicketHistory history = TicketHistory.close(ticket, actor);

        assertEquals(TicketHistoryType.CLOSED, history.getType());
        assertEquals(actor, history.getActor());
    }
}
