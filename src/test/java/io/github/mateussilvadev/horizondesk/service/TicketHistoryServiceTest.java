package io.github.mateussilvadev.horizondesk.service;

import io.github.mateussilvadev.horizondesk.exception.BusinessException;
import io.github.mateussilvadev.horizondesk.exception.Code;
import io.github.mateussilvadev.horizondesk.model.domain.TicketHistory;
import io.github.mateussilvadev.horizondesk.repository.TicketHistoryRepository;
import io.github.mateussilvadev.horizondesk.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Ticket history service test")
@ExtendWith(MockitoExtension.class)
class TicketHistoryServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private TicketHistoryRepository historyRepository;

    @InjectMocks
    private TicketHistoryService service;

    private UUID ticketUuid;

    @BeforeEach
    void setUp() {
        ticketUuid = UUID.randomUUID();
    }

    @Test
    void shouldReturnTicketHistoryWhenTicketExists() {
        TicketHistory firstHistory = mock(TicketHistory.class);
        TicketHistory secondHistory = mock(TicketHistory.class);

        List<TicketHistory> histories = List.of(firstHistory, secondHistory);

        when(ticketRepository.existsByUuid(ticketUuid)).thenReturn(true);
        when(historyRepository.findAllByTicketUuidOrderByCreatedAtAsc(ticketUuid)).thenReturn(histories);

        List<TicketHistory> result = service.findByTicketUuid(ticketUuid);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertSame(firstHistory, result.get(0));
        assertSame(secondHistory, result.get(1));

        verify(ticketRepository).existsByUuid(ticketUuid);
        verify(historyRepository)
                .findAllByTicketUuidOrderByCreatedAtAsc(ticketUuid);
    }

    @Test
    void shouldThrowBusinessExceptionWhenTicketDoesNotExist() {
        when(ticketRepository.existsByUuid(ticketUuid))
                .thenReturn(false);

        BusinessException exception = assertThrows(
                BusinessException.class, () -> service.findByTicketUuid(ticketUuid));

        assertEquals(Code.ENTITY_NOT_FOUND, exception.getCode());
        assertEquals("ticket.not_found", exception.getMessageKey());

        verify(ticketRepository).existsByUuid(ticketUuid);

        verify(historyRepository, never()).findAllByTicketUuidOrderByCreatedAtAsc(any());
    }
}
