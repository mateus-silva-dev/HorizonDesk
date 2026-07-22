package io.github.mateussilvadev.horizondesk.model.domain;

import io.github.mateussilvadev.horizondesk.model.enums.PriorityTicket;
import io.github.mateussilvadev.horizondesk.model.enums.StatusTicket;
import io.github.mateussilvadev.horizondesk.model.enums.TicketHistoryType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "ticket_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class TicketHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(nullable = false, unique = true, updatable = false, length = 36)
    private UUID uuid;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 30)
    private TicketHistoryType type;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false, updatable = false)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id", updatable = false)
    private User actor;

    @Column(updatable = false, length = 100)
    private String oldValue;

    @Column(updatable = false, length = 100)
    private String newValue;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private TicketHistory(Ticket ticket, TicketHistoryType type, User actor, String oldValue, String newValue) {
        this.uuid = UUID.randomUUID();
        this.ticket = Objects.requireNonNull(ticket);
        this.type = Objects.requireNonNull(type);
        this.actor = actor;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public static TicketHistory opened(Ticket ticket, User actor) {
        return new TicketHistory(ticket, TicketHistoryType.OPENED, actor, null, null);
    }

    public static TicketHistory assignTechnician(Ticket ticket, User actor, User technician) {
        return new TicketHistory(ticket, TicketHistoryType.TECHNICIAN_ASSIGNED, actor, null, technician.getName());
    }

    public static TicketHistory changePriority(Ticket ticket, User actor, PriorityTicket oldPriority, PriorityTicket newPriority) {
        return new TicketHistory(ticket, TicketHistoryType.PRIORITY_CHANGED, actor, oldPriority.name(), newPriority.name());
    }

    public static TicketHistory update(Ticket ticket, User actor) {
        return new TicketHistory(ticket, TicketHistoryType.UPDATED, actor, null, null);
    }

    public static TicketHistory resolve(Ticket ticket, User technician) {
        return new TicketHistory(ticket, TicketHistoryType.RESOLVED, technician, null, null);
    }

    public static TicketHistory reopen(Ticket ticket, User actor) {
        return new TicketHistory(ticket, TicketHistoryType.REOPENED, actor, null, null);
    }

    public static TicketHistory close(Ticket ticket, User actor) {
        return new TicketHistory(ticket, TicketHistoryType.CLOSED, actor, null, null);
    }
}