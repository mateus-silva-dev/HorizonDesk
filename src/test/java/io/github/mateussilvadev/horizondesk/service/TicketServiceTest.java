package io.github.mateussilvadev.horizondesk.service;

import io.github.mateussilvadev.horizondesk.builder.TicketBuilder;
import io.github.mateussilvadev.horizondesk.builder.UserBuilder;
import io.github.mateussilvadev.horizondesk.dto.request.TicketRequestDTOs;
import io.github.mateussilvadev.horizondesk.exception.EntityNotFoundException;
import io.github.mateussilvadev.horizondesk.model.domain.Ticket;
import io.github.mateussilvadev.horizondesk.model.domain.User;
import io.github.mateussilvadev.horizondesk.model.enums.PriorityTicket;
import io.github.mateussilvadev.horizondesk.repository.TicketRepository;
import io.github.mateussilvadev.horizondesk.repository.UserRepository;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import static io.github.mateussilvadev.horizondesk.exception.Code.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@DisplayName("Ticket service test")
@ExtendWith(MockitoExtension.class)
public class TicketServiceTest {

    private static final Faker faker = new Faker(Locale.of("pt", "BR"));

    @Mock
    private TicketRepository repository;

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

        service = new TicketService(repository, userRepository);
    }

    private Ticket mockTicketFound(UUID uuid) {
        var ticket = TicketBuilder.anTicket().withUuid(uuid).build();
        given(repository.findByUuid(uuid)).willReturn(Optional.of(ticket));
        return ticket;
    }

    private User mockUserFound(UUID uuid) {
        var user = UserBuilder.anUser().withUuid(uuid).build();
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
    @DisplayName("Search test for ticket")
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
    @DisplayName("Update test for ticket")
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
}
