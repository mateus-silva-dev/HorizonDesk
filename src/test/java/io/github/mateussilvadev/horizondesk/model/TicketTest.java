package io.github.mateussilvadev.horizondesk.model;

import io.github.mateussilvadev.horizondesk.builder.TicketBuilder;
import io.github.mateussilvadev.horizondesk.builder.UserBuilder;
import io.github.mateussilvadev.horizondesk.exception.BusinessException;
import io.github.mateussilvadev.horizondesk.model.domain.Ticket;
import io.github.mateussilvadev.horizondesk.model.domain.User;
import io.github.mateussilvadev.horizondesk.model.enums.PriorityTicket;
import io.github.mateussilvadev.horizondesk.model.enums.Role;
import io.github.mateussilvadev.horizondesk.model.enums.StatusTicket;
import io.github.mateussilvadev.horizondesk.support.DomainAssertions;
import jakarta.persistence.PostPersist;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Ticket entity domain rules test")
public class TicketTest implements DomainAssertions {

    private static final Faker faker = new Faker(Locale.of("pt", "BR"));

    private Ticket ticket;
    private TicketBuilder builder;
    private User user;

    @BeforeEach
    void setUp() {
        this.builder = TicketBuilder.anTicket();
        this.ticket = builder.build();
        this.user = UserBuilder.anUser().withRole(Role.ROLE_TECHNICIAN).build();
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

    @Test
    @DisplayName("Should generate final title matching the incremental database ID")
    void shouldGenerateFinalTitleWithId() {
        Ticket ticket = TicketBuilder.anTicket().build();
        assertThat(ticket.getTitle()).isEqualTo("Ticket #PENDING");

        ReflectionTestUtils.setField(ticket, "id", 45L);

        ticket.generateFinalTitle();

        assertThat(ticket.getTitle()).isEqualTo("Ticket #45");
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

    @Nested
    @DisplayName("Change priority for ticket")
    class ChangePriority {

        @ParameterizedTest(name = "Priority: {0}")
        @ValueSource(strings = {"LOW", "MEDIUM", "HIGH", "CRITICAL"})
        @DisplayName("Should change priority successfully")
        void shouldChangePriority(String priority) {
            PriorityTicket priorityTicket = PriorityTicket.valueOf(priority);
            assertUpdateWorkflow(ticket::changePriority, ticket::getPriority, priorityTicket, priorityTicket);
        }

        @Test
        @DisplayName("Should ignore department update when entity is identical to current")
        void shouldIgnoreUpdateWhenDepartmentIsTheSame() {
            assertNoChange(() -> ticket.changePriority(ticket.getPriority()), ticket::getPriority);
        }

        @ParameterizedTest(name = "Priority: {0}")
        @ValueSource(strings = {"LOW", "MEDIUM", "HIGH", "CRITICAL"})
        @DisplayName("It should throw an exception when attempting to change priority a ticket with a status other than open")
        void shouldThrowExceptionWhenTicketNotIsOpen(String priority) {
            Ticket ticket1 = TicketBuilder.anTicket().withStatus(StatusTicket.IN_PROGRESS).build();
            PriorityTicket priorityTicket = PriorityTicket.valueOf(priority);
            assertThatException(
                    () -> ticket1.changePriority(priorityTicket),
                    BusinessException.class, "ticket.error.ticket_already_closed"
            );
        }

    }

    @Nested
    @DisplayName("Assigns a technician to a ticket")
    class AssignTechnician {
        @Test
        @DisplayName("You must assign a technician to a ticket")
        void shouldAssignTechnician() {
            User technician = UserBuilder.anUser().withRole(Role.ROLE_TECHNICIAN).build();
            Ticket ticket = TicketBuilder.anTicket().withStatus(StatusTicket.OPEN).build();

            ticket.assignTechnician(technician);

            assertThat(ticket.getStatus()).isEqualTo(StatusTicket.IN_PROGRESS);
            assertThat(ticket.getAssignedTechnician()).isEqualTo(technician);
        }

        @Test
        @DisplayName("You must assign a technician to a ticket")
        void shouldThrowExceptionWhenUserIsNotATechnician() {
            User notTechnician = UserBuilder.anUser().withRole(Role.ROLE_CUSTOMER).build();
            assertThatException(
                    () -> ticket.assignTechnician(notTechnician),
                    BusinessException.class, "ticket.error.invalid_technician"
            );
        }

        @Test
        @DisplayName("should Throw Exception When Technician Accepts Their Own Ticket")
        void shouldThrowExceptionWhenTechnicianAcceptsTheirOwnTicket() {
            User technician = UserBuilder.anUser().withRole(Role.ROLE_TECHNICIAN).build();
            Ticket ticket = TicketBuilder.anTicket().withRequester(technician).build();
            assertThatException(
                    () -> ticket.assignTechnician(technician),
                    BusinessException.class, "ticket.error.technician_cannot_be_requester"
            );
        }

        @Test
        @DisplayName("It should not change the status if the ticket is assigned to another technician")
        void shouldNotChangeStatusWhenTechnicianIsAlreadyAssigned() {
            User technician = UserBuilder.anUser().withRole(Role.ROLE_TECHNICIAN).build();
            Ticket ticket = TicketBuilder.anTicket().withStatus(StatusTicket.IN_PROGRESS).withAssignedTechnician(technician).build();
            ticket.assignTechnician(technician);
            assertThat(ticket.getStatus()).isEqualTo(StatusTicket.IN_PROGRESS);
        }

        @ParameterizedTest
        @EnumSource(value = StatusTicket.class, names = {"OPEN", "IN_PROGRESS"})
        @DisplayName("It must allow the technician to be changed when the ticket is OPEN or IN_PROGRESS")
        void shouldAllowTechnicianChangeWhenTicketIsOpenOrInProgress(StatusTicket initialStatus) {
            User technician = UserBuilder.anUser().withRole(Role.ROLE_TECHNICIAN).build();
            Ticket ticket = TicketBuilder.anTicket().withStatus(initialStatus).build();

            ticket.assignTechnician(technician);

            assertThat(ticket.getAssignedTechnician()).isEqualTo(technician);
            assertThat(ticket.getStatus()).isEqualTo(StatusTicket.IN_PROGRESS);
        }

        @ParameterizedTest
        @EnumSource(value = StatusTicket.class, names = {"RESOLVED", "CLOSED"})
        @DisplayName("It should throw an exception when attempting to change the technician of a ticket with a status other than OPEN or IN_PROGRESS")
        void shouldThrowExceptionWhenTicketNotIsOpenOrInProgress(StatusTicket initialStatus) {
            Ticket ticket = TicketBuilder.anTicket().withStatus(initialStatus).build();
            User technician = UserBuilder.anUser().withRole(Role.ROLE_TECHNICIAN).build();
            assertThatException(
                    () -> ticket.assignTechnician(technician),
                    BusinessException.class, "ticket.error.ticket_already_closed"
            );
        }

    }

    @Nested
    @DisplayName("Change status of ticket")
    class ChangeStatus {
        @ParameterizedTest
        @EnumSource(value = StatusTicket.class, names = {"OPEN", "CLOSED", "RESOLVED"})
        @DisplayName("Should throw exception when trying to resolve a ticket from an invalid status")
        void shouldThrowExceptionWhenResolvingFromInvalidStatus(StatusTicket invalidStatus) {
            Ticket ticket = TicketBuilder.anTicket().withStatus(invalidStatus).build();
            assertThatException(
                    () -> ticket.resolve(), BusinessException.class, "ticket.error.invalid_status_transition"
            );
        }

        @Test
        @DisplayName("Should update status to RESOLVED when resolve is called from IN_PROGRESS")
        void shouldUpdateStatusToResolved() {
            Ticket ticket = TicketBuilder.anTicket().withStatus(StatusTicket.IN_PROGRESS).build();
            ticket.resolve();
            assertThat(ticket.getStatus()).isEqualTo(StatusTicket.RESOLVED);
        }

        @Test
        @DisplayName("Should update status to IN_PROGRESS when resolve is called from RESOLVED")
        void shouldUpdateStatusToInProgress() {
            Ticket ticket = TicketBuilder.anTicket().withStatus(StatusTicket.RESOLVED).build();
            ticket.reopen();
            assertThat(ticket.getStatus()).isEqualTo(StatusTicket.IN_PROGRESS);
        }

        @Test
        @DisplayName("Should update status to CLOSED when resolve is called from IN_PROGRESS")
        void shouldUpdateStatusToClosed() {
            Ticket ticket = TicketBuilder.anTicket().withStatus(StatusTicket.RESOLVED).build();
            ticket.close();
            assertThat(ticket.getStatus()).isEqualTo(StatusTicket.CLOSED);
        }
    }
}
