package io.github.mateussilvadev.horizondesk.service;

import io.github.mateussilvadev.horizondesk.builder.TicketBuilder;
import io.github.mateussilvadev.horizondesk.builder.UserBuilder;
import io.github.mateussilvadev.horizondesk.dto.request.TicketFilter;
import io.github.mateussilvadev.horizondesk.dto.request.TicketRequestDTOs;
import io.github.mateussilvadev.horizondesk.exception.BusinessException;
import io.github.mateussilvadev.horizondesk.exception.EntityNotFoundException;
import io.github.mateussilvadev.horizondesk.model.domain.Ticket;
import io.github.mateussilvadev.horizondesk.model.domain.User;
import io.github.mateussilvadev.horizondesk.model.enums.PriorityTicket;
import io.github.mateussilvadev.horizondesk.model.enums.Role;
import io.github.mateussilvadev.horizondesk.model.enums.StatusTicket;
import io.github.mateussilvadev.horizondesk.repository.TicketHistoryRepository;
import io.github.mateussilvadev.horizondesk.repository.TicketRepository;
import io.github.mateussilvadev.horizondesk.repository.UserRepository;
import io.github.mateussilvadev.horizondesk.support.DomainAssertions;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import static io.github.mateussilvadev.horizondesk.exception.Code.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@DisplayName("Ticket service test")
@ExtendWith(MockitoExtension.class)
public class TicketServiceTest implements DomainAssertions {

    private static final Faker faker = new Faker(Locale.of("pt", "BR"));

    @Mock
    private TicketRepository repository;

    @Mock
    private TicketHistoryRepository historyRepository;

    @Mock
    private UserRepository userRepository;

    private TicketService service;

    @InjectMocks
    private TicketRequestDTOs.TicketCreate createDTO;
    private final UUID uuid = UUID.randomUUID();
    private final UUID userUuid = UUID.fromString("b85a4dd5-0866-40d2-9688-c5ba16ce9b5e");

    @BeforeEach
    void setUp() {
        createDTO = new TicketRequestDTOs.TicketCreate(
                faker.lorem().characters(150),
                PriorityTicket.LOW, userUuid
        );

        service = new TicketService(repository, userRepository, historyRepository);
    }

    private Ticket mockTicketFound(UUID uuid) {
        var ticket = TicketBuilder.anTicket().withUuid(uuid).build();
        given(repository.findByUuid(uuid)).willReturn(Optional.of(ticket));
        return ticket;
    }

    private User mockUserFound(UUID uuid) {
        var user = UserBuilder.anUser().withUuid(uuid).withRole(Role.ROLE_TECHNICIAN).build();
        given(userRepository.findByUuid(uuid)).willReturn(Optional.of(user));
        return user;
    }

    @Nested
    @DisplayName("Ticket creation test")
    class Create {

        @Test
        @DisplayName("Create should save ticket")
        void shouldSaveTicket() {
            User user = mockUserFound(userUuid);

            given(repository.saveAndFlush(any(Ticket.class))).willAnswer(i -> {
                Ticket ticket = i.getArgument(0);
                ReflectionTestUtils.setField(ticket, "id", 1L);
                return ticket;
            });

            given(repository.save(any(Ticket.class))).willAnswer(i -> i.getArgument(0));

            var answer = service.create(createDTO);

            assertThat(answer).isNotNull();
            assertThat(answer.getTitle()).isEqualTo("Ticket #1");
            assertThat(answer).
                    extracting("description", "priority", "requester")
                    .containsExactly(createDTO.description(), createDTO.priorityTicket(), user);
        }

        @Test
        @DisplayName("An exception should be thrown when the user not exist")
        void shouldThrowExceptionWhenUserNotFound() {
            given(userRepository.findByUuid(createDTO.requesterUuid())).willReturn(Optional.empty());

            EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () -> service.create(createDTO));

            assertThat(ex.getCode()).isEqualTo(ENTITY_NOT_FOUND);
            assertThat(ex.getMessage()).isEqualTo("ticket_service.error.requester_not_found");
        }

    }

    @Nested
    @DisplayName("Test to search the ticket")
    class Find {
        @Test
        @DisplayName("Search by UUID")
        void byUUID() {
            var ticket = mockTicketFound(uuid);

            var answer = service.findByUuid(uuid);

            assertThat(answer).isNotNull();
            assertThat(answer.getUuid()).isEqualTo(ticket.getUuid());

            verify(repository).findByUuid(uuid);
        }

        @Test
        @DisplayName("An exception should be thrown when the ticket is not found")
        void shouldThrowExceptionWhenTicketNotFound() {
            UUID uuid = UUID.randomUUID();
            given(repository.findByUuid(uuid)).willReturn(Optional.empty());

            EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () -> service.findByUuid(uuid));

            assertThat(ex.getCode()).isEqualTo(ENTITY_NOT_FOUND);
            assertThat(ex.getMessage()).isEqualTo("ticket_service.error.ticket_not_found");

            verify(repository).findByUuid(uuid);
        }
    }

    @Nested
    @DisplayName("Test to update the ticket")
    class Update {
        @Test
        void shouldUpdateTicket() {
            Ticket ticket = mockTicketFound(uuid);
            TicketRequestDTOs.TicketUpdate ticketUpdateDTO = new TicketRequestDTOs.TicketUpdate(faker.lorem().characters(100));

            var answer = service.updateTicket(uuid, ticketUpdateDTO);

            assertThat(answer).isNotNull();
            assertThat(answer.getUuid()).isEqualTo(ticket.getUuid());
            assertThat(answer.getDescription()).isEqualTo(ticketUpdateDTO.description());
        }
    }

    @Nested
    @DisplayName("Test to change the ticket priority")
    class ChangePriority {

        @ParameterizedTest(name = "Priority: {0}")
        @ValueSource(strings = {"LOW", "MEDIUM", "HIGH", "CRITICAL"})
        void shouldChangePriority(String priority) {
            Ticket ticket = mockTicketFound(uuid);
            service.changePriority(uuid, PriorityTicket.valueOf(priority));
            assertThat(ticket.getPriority()).isEqualTo(PriorityTicket.valueOf(priority));
        }

    }

    @Nested
    @DisplayName("Tests the assignment of a technician to a ticket")
    class AssignTechnician {

        @Test
        @DisplayName("You must assign a technician to a ticket.")
        void shouldAssignTechnician() {
            Ticket ticket = mockTicketFound(uuid);
            User technician = mockUserFound(userUuid);
            service.assignTechnician(uuid, userUuid);
            assertThat(ticket.getAssignedTechnician()).isEqualTo(technician);
        }

        @Test
        @DisplayName("An exception should be thrown when the technician is not found")
        void shouldThrowExceptionWhenTechnicianNotFound() {
            UUID random = UUID.randomUUID();
            Ticket ticket = mockTicketFound(uuid);
            given(userRepository.findByUuid(random)).willReturn(Optional.empty());

            assertThatException(
                    () -> service.assignTechnician(uuid, random),
                    EntityNotFoundException.class, "ticket_service.error.technician_not_found");

            verify(repository, never()).save(any());
        }

    }

    @Nested
    @DisplayName("Tests the resolution of a ticket")
    class ChangeStatusTicket {

        @Test
        @DisplayName("Resolve should update status and save ticket")
        void shouldResolveTicketAndSave() {
            Ticket ticket = TicketBuilder.anTicket().withStatus(StatusTicket.IN_PROGRESS).build();
            given(repository.findByUuid(uuid)).willReturn(Optional.of(ticket));
            service.resolveTicket(uuid);
            assertThat(ticket.getStatus()).isEqualTo(StatusTicket.RESOLVED);
        }

        @Test
        @DisplayName("Reopen should update status and save ticket")
        void shouldReopenTicketAndSave() {
            Ticket ticket = TicketBuilder.anTicket().withStatus(StatusTicket.RESOLVED).build();
            given(repository.findByUuid(uuid)).willReturn(Optional.of(ticket));
            service.reopenTicket(uuid);
            assertThat(ticket.getStatus()).isEqualTo(StatusTicket.IN_PROGRESS);
        }

        @Test
        @DisplayName("Close should update status and save ticket")
        void shouldCloseTicketAndSave() {
            Ticket ticket = TicketBuilder.anTicket().withStatus(StatusTicket.RESOLVED).build();
            given(repository.findByUuid(uuid)).willReturn(Optional.of(ticket));
            service.closeTicket(uuid);
            assertThat(ticket.getStatus()).isEqualTo(StatusTicket.CLOSED);
        }

    }

    @Nested
    @DisplayName("Tests the search of a ticket")
    class Search {

        @Test
        @DisplayName("Should return paginated tickets when filters are applied")
        void shouldReturnPaginatedTicketsWhenFiltersAreApplied() {
            TicketFilter filter = new TicketFilter(StatusTicket.OPEN, PriorityTicket.HIGH, null, null, null);
            Pageable pageable = PageRequest.of(0, 20);

            List<Ticket> tickets = List.of(TicketBuilder.anTicket().build());
            Page<Ticket> pageMock = new PageImpl<>(tickets, pageable, tickets.size());

            given(repository.findAll(any(Specification.class), eq(pageable))).willReturn(pageMock);

            Page<Ticket> result = service.findAllPaginated(filter, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);

            verify(repository, times(1)).findAll(any(Specification.class), eq(pageable));
        }

    }
}
