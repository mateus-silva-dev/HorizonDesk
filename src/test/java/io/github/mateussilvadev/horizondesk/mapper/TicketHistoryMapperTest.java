package io.github.mateussilvadev.horizondesk.mapper;

import io.github.mateussilvadev.horizondesk.dto.response.TicketHistoryResponse;
import io.github.mateussilvadev.horizondesk.model.domain.Ticket;
import io.github.mateussilvadev.horizondesk.model.domain.TicketHistory;
import io.github.mateussilvadev.horizondesk.model.domain.User;
import io.github.mateussilvadev.horizondesk.model.enums.PriorityTicket;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("TicketHistory mapper test")
@ExtendWith(MockitoExtension.class)
class TicketHistoryMapperTest {

    private TicketHistoryMapper mapper;

    private Ticket ticket;
    private User actor;
    private User technician;

    @BeforeEach
    void setUp() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();

        messageSource.setBasename("i18n/messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setUseCodeAsDefaultMessage(false);

        mapper = new TicketHistoryMapper(messageSource);

        LocaleContextHolder.setLocale(Locale.ENGLISH);

        ticket = mock(Ticket.class);
        actor = mock(User.class);
        technician = mock(User.class);
    }

    @AfterEach
    void tearDown() {
        LocaleContextHolder.resetLocaleContext();
    }

    @Test
    void shouldReturnNullWhenHistoryIsNull() {
        TicketHistoryResponse response = mapper.toResponse(null);
        assertNull(response);
    }

    @Test
    void shouldMapOpenedHistory() {
        when(actor.getName()).thenReturn("Ana Clara");
        TicketHistory history = TicketHistory.opened(ticket, actor);
        TicketHistoryResponse response = mapper.toResponse(history);

        assertNotNull(response);
        assertEquals("Ticket opened by Ana Clara", response.message());
        assertEquals("Ana Clara", response.actorName());
        assertEquals(history.getUuid(), response.uuid());
    }

    @Test
    void shouldMapUpdatedHistory() {
        when(actor.getName()).thenReturn("Ana Clara");
        TicketHistory history = TicketHistory.update(ticket, actor);

        TicketHistoryResponse response = mapper.toResponse(history);

        assertNotNull(response);
        assertEquals("Ticket updated by Ana Clara", response.message());
        assertEquals("Ana Clara", response.actorName());
    }

    @Test
    void shouldMapPriorityChangedHistory() {
        when(actor.getName()).thenReturn("Ana Clara");
        TicketHistory history = TicketHistory.changePriority(ticket, actor, PriorityTicket.LOW, PriorityTicket.HIGH);
        TicketHistoryResponse response = mapper.toResponse(history);

        assertNotNull(response);
        assertEquals("Priority changed from Low to High by Ana Clara", response.message());
        assertEquals("Ana Clara", response.actorName());
    }

    @Test
    void shouldMapTechnicianAssignedWithoutActor() {
        when(technician.getName()).thenReturn("Maria Alice");

        TicketHistory history = TicketHistory.assignTechnician(ticket, null, technician);
        TicketHistoryResponse response = mapper.toResponse(history);

        assertNotNull(response);
        assertEquals("Ticket assigned to technician Maria Alice", response.message());
        assertNull(response.actorName());
    }

    @Test
    void shouldMapTechnicianAssignedWithActor() {
        when(technician.getName()).thenReturn("Maria Alice");
        when(actor.getName()).thenReturn("Ana Clara");

        TicketHistory history = TicketHistory.assignTechnician(ticket, actor, technician);
        TicketHistoryResponse response = mapper.toResponse(history);

        assertNotNull(response);
        assertEquals("Ticket assigned to technician Maria Alice by Ana Clara", response.message());
        assertEquals("Ana Clara", response.actorName());
    }

    @Test
    void shouldMapReopenedHistory() {
        when(actor.getName()).thenReturn("Ana Clara");
        TicketHistory history = TicketHistory.reopen(ticket, actor);

        TicketHistoryResponse response = mapper.toResponse(history);

        assertNotNull(response);
        assertEquals("Ticket reopened by Ana Clara", response.message());
        assertEquals("Ana Clara", response.actorName());
    }

    @Test
    void shouldMapResolvedHistory() {
        when(technician.getName()).thenReturn("Maria Alice");

        TicketHistory history = TicketHistory.resolve(ticket, technician);
        TicketHistoryResponse response = mapper.toResponse(history);

        assertNotNull(response);
        assertEquals("Ticket resolved by Maria Alice", response.message());
        assertEquals("Maria Alice", response.actorName());
    }

    @Test
    void shouldMapClosedHistory() {
        when(actor.getName()).thenReturn("Ana Clara");
        TicketHistory history = TicketHistory.close(ticket, actor);

        TicketHistoryResponse response = mapper.toResponse(history);

        assertNotNull(response);
        assertEquals("Ticket closed by Ana Clara", response.message());
        assertEquals("Ana Clara", response.actorName());
    }
}
