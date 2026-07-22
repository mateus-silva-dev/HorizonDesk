package io.github.mateussilvadev.horizondesk.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mateussilvadev.horizondesk.builder.TicketBuilder;
import io.github.mateussilvadev.horizondesk.builder.UserBuilder;
import io.github.mateussilvadev.horizondesk.dto.request.TicketFilter;
import io.github.mateussilvadev.horizondesk.dto.request.TicketRequestDTOs;
import io.github.mateussilvadev.horizondesk.dto.response.TicketHistoryResponse;
import io.github.mateussilvadev.horizondesk.exception.BusinessException;
import io.github.mateussilvadev.horizondesk.exception.Code;
import io.github.mateussilvadev.horizondesk.exception.EntityNotFoundException;
import io.github.mateussilvadev.horizondesk.mapper.TicketHistoryMapper;
import io.github.mateussilvadev.horizondesk.model.domain.Ticket;
import io.github.mateussilvadev.horizondesk.model.domain.TicketHistory;
import io.github.mateussilvadev.horizondesk.model.domain.User;
import io.github.mateussilvadev.horizondesk.model.enums.PriorityTicket;
import io.github.mateussilvadev.horizondesk.model.enums.TicketHistoryType;
import io.github.mateussilvadev.horizondesk.service.TicketHistoryService;
import io.github.mateussilvadev.horizondesk.service.TicketService;
import net.datafaker.Faker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static io.github.mateussilvadev.horizondesk.model.enums.PriorityTicket.HIGH;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TicketController.class)
@DisplayName("TicketController test")
public class TicketControllerTest {

    private static final Faker faker = new Faker(Locale.of("pt", "BR"));
    private static final String BASE_URL = "/api/v1/tickets";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TicketService service;

    @MockitoBean
    private TicketHistoryService historyService;

    @MockitoBean
    private TicketHistoryMapper mapper;

    @MockitoBean
    private MessageSource messageSource;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final UUID uuid = UUID.randomUUID();
    private final User mockUser = UserBuilder.anUser().build();

    @Test
    @DisplayName("Should return 500 when an unexpected error occurs")
    void shouldReturn500OnGenericException() throws Exception {
        UUID randomUuid = UUID.randomUUID();

        given(service.findByUuid(any(UUID.class)))
                .willThrow(new RuntimeException("error.internal_server_error"));

        mockMvc.perform(get(BASE_URL + "/{uuid}", randomUuid))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(Code.INTERNAL_SERVER_ERROR.toString()));
    }

    @Test
    @DisplayName("Should return 400 for general business rules")
    void shouldReturn400ForGeneralBusinessException() throws Exception {
        var request = new TicketRequestDTOs.TicketCreate(faker.lorem().characters(150), PriorityTicket.LOW, mockUser.getUuid());

        given(service.create(any(TicketRequestDTOs.TicketCreate.class))).willThrow(
                new BusinessException(Code.MALFORMED_JSON, "error.invalid_json"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(Code.MALFORMED_JSON.toString()));
    }

    @Test
    @DisplayName("Should return 422 when any data is invalid.")
    void shouldReturn422WhenAnyDataIsInvalid() throws Exception {
        var request = new TicketRequestDTOs.TicketCreate(null, PriorityTicket.LOW, mockUser.getUuid());

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value(Code.VALIDATION_ERROR.toString()));

        verifyNoInteractions(service);
    }

    @Test
    @DisplayName("Should return 404 when UUID does not exist")
    void shouldReturn404WhenEntityNotFound() throws Exception {
        UUID randomUuid = UUID.randomUUID();
        given(service.findByUuid(randomUuid))
                .willThrow(new EntityNotFoundException("ticket_service.error.ticket_not_found"));

        mockMvc.perform(get(BASE_URL + "/{uuid}", randomUuid))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(Code.ENTITY_NOT_FOUND.toString()));
    }

    @Nested
    @DisplayName("Create ticket")
    class TicketCreate {
        @Test
        @DisplayName("Should create ticket and return 201")
        void shouldCreateTicket() throws Exception {
            var request = new TicketRequestDTOs.TicketCreate(faker.lorem().characters(150), PriorityTicket.LOW, mockUser.getUuid());

            var ticket = TicketBuilder.anTicket()
                    .withDescription(request.description())
                    .withPriority(request.priorityTicket())
                    .withRequester(mockUser)
                    .build();

            given(service.create(any())).willReturn(ticket);

            ResultActions actions = mockMvc.perform(post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            assertTicketResponse(actions, ticket);
        }
    }

    @Test
    @DisplayName("Should get ticket by UUID and return 200")
    void shouldGetTicketByUuid() throws Exception {
        var ticket = TicketBuilder.anTicket().withUuid(uuid).build();
        given(service.findByUuid(uuid)).willReturn(ticket);

        mockMvc.perform(get(BASE_URL + "/{uuid}", uuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(ticket.getUuid().toString()));
    }

    @Test
    @DisplayName("Should update user and return 200")
    void shouldUpdateUser() throws Exception {
        var dto = new TicketRequestDTOs.TicketUpdate("New Description #1");
        var ticket = TicketBuilder.anTicket().withDescription("New Description #1").build();

        given(service.updateTicket(eq(uuid), any())).willReturn(ticket);

        mockMvc.perform(patch(BASE_URL + "/{uuid}", uuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value(ticket.getDescription()));
    }

    @Test
    @DisplayName("Should change user role and return 204")
    void shouldChangePriority() throws Exception {
        var dto = new TicketRequestDTOs.TicketChangePriority(HIGH);

        mockMvc.perform(patch(BASE_URL + "/{uuid}/priority", uuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Must assign a technician to a ticket. and return 204")
    void shouldAssignTechnicianTheTicket() throws Exception {
        var dto = new TicketRequestDTOs.TicketAssignTechnician(UUID.randomUUID());

        mockMvc.perform(patch(BASE_URL + "/{uuid}/assign", uuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNoContent());
    }

    @ParameterizedTest
    @ValueSource(strings = {"resolve", "reopen", "close"})
    @DisplayName("Should return 204 when changing ticket status")
    void shouldReturn204WhenChangingStatus(String action) throws Exception {
        mockMvc.perform(patch(BASE_URL + "/{uuid}/" + action, uuid))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /api/v1/tickets should return 200 OK and paginated response")
    void getTicketsShouldReturnPaginatedResponse() throws Exception {
        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "priority", "createdAt"));
        List<Ticket> tickets = List.of(TicketBuilder.anTicket().build());
        Page<Ticket> pageMock = new PageImpl<>(tickets, pageable, tickets.size());

        given(service.findAllPaginated(any(TicketFilter.class), any(Pageable.class))).willReturn(pageMock);

        mockMvc.perform(get("/api/v1/tickets")
                        .param("status", "OPEN")
                        .param("priority", "HIGH")
                        .param("page", "0")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.last").value(true));

        verify(service, times(1)).findAllPaginated(any(TicketFilter.class), any(Pageable.class));
    }

    @Test
    @DisplayName("Should return 200 and list of comments")
    void shouldReturnTicketHistory() throws Exception {
        TicketHistory firstHistory = mock(TicketHistory.class);
        TicketHistory secondHistory = mock(TicketHistory.class);

        List<TicketHistory> histories = List.of(firstHistory, secondHistory);

        UUID firstUuid = UUID.randomUUID();
        UUID secondUuid = UUID.randomUUID();
        UUID actorUuid = UUID.randomUUID();

        TicketHistoryResponse firstResponse =
                new TicketHistoryResponse(
                        firstUuid, TicketHistoryType.OPENED, "Ticket opened by Ana Clara", actorUuid,
                        "Ana Clara", LocalDateTime.of(2026, 7, 23, 10, 0));

        TicketHistoryResponse secondResponse =
                new TicketHistoryResponse(secondUuid,
                        TicketHistoryType.TECHNICIAN_ASSIGNED, "Ticket assigned to technician Maria Alice", null, null,
                        LocalDateTime.of(2026, 7, 23, 10, 5));

        when(historyService.findByTicketUuid(uuid))
                .thenReturn(histories);

        when(mapper.toResponseList(histories))
                .thenReturn(List.of(firstResponse, secondResponse));

        mockMvc.perform(get(BASE_URL + "/{uuid}/history", uuid)
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))


                .andExpect(jsonPath("$[0].uuid").value(firstUuid.toString()))
                .andExpect(jsonPath("$[0].type").value("OPENED"))
                .andExpect(jsonPath("$[0].message").value("Ticket opened by Ana Clara"))
                .andExpect(jsonPath("$[0].actorUuid").value(actorUuid.toString()))
                .andExpect(jsonPath("$[0].actorName").value("Ana Clara"))
                .andExpect(jsonPath("$[0].createdAt").exists())

                .andExpect(jsonPath("$[1].uuid").value(secondUuid.toString()))
                .andExpect(jsonPath("$[1].type").value("TECHNICIAN_ASSIGNED"))
                .andExpect(jsonPath("$[1].message").value("Ticket assigned to technician Maria Alice"))
                .andExpect(jsonPath("$[1].actorUuid").doesNotExist())
                .andExpect(jsonPath("$[1].actorName").doesNotExist())
                .andExpect(jsonPath("$[1].createdAt").exists());

        verify(historyService).findByTicketUuid(uuid);
        verify(mapper).toResponseList(histories);
    }


    private void assertTicketResponse(ResultActions actions, Ticket ticket) throws Exception {
        actions
                .andExpect(jsonPath("$.uuid").value(ticket.getUuid().toString()))
                .andExpect(jsonPath("$.title").value(ticket.getTitle()))
                .andExpect(jsonPath("$.description").value(ticket.getDescription()))
                .andExpect(jsonPath("$.priority").value(ticket.getPriority().name()))
                .andExpect(jsonPath("$.status").value(ticket.getStatus().name()))
                .andExpect(jsonPath("$.user").value(mockUser.getName()))
                .andExpect(jsonPath("$.department").value(mockUser.getDepartment().getName()))
                .andExpect(jsonPath("$.createdAt").value(ticket.getCreatedAt()));
    }
}
