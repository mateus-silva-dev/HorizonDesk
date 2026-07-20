package io.github.mateussilvadev.horizondesk.model;

import io.github.mateussilvadev.horizondesk.builder.TicketBuilder;
import io.github.mateussilvadev.horizondesk.exception.BusinessException;
import io.github.mateussilvadev.horizondesk.model.domain.Ticket;
import io.github.mateussilvadev.horizondesk.model.enums.StatusTicket;
import io.github.mateussilvadev.horizondesk.support.DomainAssertions;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Ticket entity domain rules test")
public class TicketTest implements DomainAssertions {

    private static final Faker faker = new Faker(Locale.of("pt", "BR"));

    private Ticket ticket;
    private TicketBuilder builder;

    @BeforeEach
    void setUp() {
        this.builder = TicketBuilder.anTicket();
        this.ticket = builder.build();
    }

    @Nested
    @DisplayName("Ticket creation tests")
    class TicketCreate {
        @Test
        @DisplayName("Should validate initial entity state match builder inputs")
        void shouldCreateTicketWithValidateState() {
            assertEntityState(ticket, Map.of(
                    "title", builder.getTitle(),
                    "description", builder.getDescription(),
                    "priority", builder.getPriority(),
                    "priorityWeight", builder.getPriorityWeight(),
                    "requester", builder.getRequester(),
                    "department", builder.getDepartment(),
                    "status", builder.getStatus()
            ));

            assertNotNull(ticket.getUuid());
        }

        @Test
        @DisplayName("Should throw exception when inputs violate core domain rules")
        void shouldThrowExceptionWhenInputsAreInvalid() {
            assertAll(
                    () -> assertThatException(
                            () -> TicketBuilder.anTicket().withDescription(null).build(),
                            BusinessException.class, "validation.field_min_length"),
                    () -> assertThatException(
                            () -> TicketBuilder.anTicket().withDescription("Curt").build(),
                            BusinessException.class, "validation.field_min_length"),
                    () -> assertThatException(
                            () -> TicketBuilder.anTicket().withDescription("A".repeat(201)).build(),
                            BusinessException.class, "ticket.error.invalid_description"));

        }

    }

    @Nested
    @DisplayName("Ticket update tests")
    class TicketUpdate {

        @Test
        @DisplayName("Should update ticket successfully")
        void shouldUpdateTicket() {
            String newDescription = "New description valid";
            assertUpdateWorkflow(ticket::changeDescription, ticket::getDescription, newDescription, newDescription);
        }

        @Test
        @DisplayName("It should throw an exception when attempting to edit a ticket with a status other than open")
        void shouldThrowExceptionWhenTicketNotIsOpen() {
            Ticket ticket1 = TicketBuilder.anTicket().withStatus(StatusTicket.IN_PROGRESS).build();
            assertThatException(
                    () -> ticket1.changeDescription("New description"),
                    BusinessException.class, "ticket.error.ticket_already_closed"
            );
        }
    }
}
