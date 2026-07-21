package io.github.mateussilvadev.horizondesk.model.domain;

import io.github.mateussilvadev.horizondesk.exception.BusinessException;
import io.github.mateussilvadev.horizondesk.exception.Code;
import io.github.mateussilvadev.horizondesk.model.enums.PriorityTicket;
import io.github.mateussilvadev.horizondesk.model.enums.Role;
import io.github.mateussilvadev.horizondesk.model.enums.StatusTicket;
import io.github.mateussilvadev.horizondesk.validation.CommonValidation;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tickets")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = false, of = "uuid")
@EntityListeners(AuditingEntityListener.class)
public class Ticket implements Serializable {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false, length = 36)
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID uuid;

    @Column(nullable = false, unique = true, length = 20)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(nullable = false, length = 25)
    @Enumerated(EnumType.STRING)
    private PriorityTicket priority;

    @Column(nullable = false)
    private int priorityWeight;

    @Column(nullable = false, length = 25)
    @Enumerated(EnumType.STRING)
    private StatusTicket status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_technician_id")
    private User assignedTechnician;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @LastModifiedBy
    private String updatedBy;

    @CreatedBy
    private String createdBy;

    private Ticket(String description, PriorityTicket priority, User requester) {
        this.uuid = UUID.randomUUID();
        this.title = "Ticket #PENDING";
        this.description = checkDescription(description);
        this.priority = priority;
        this.priorityWeight = priority.getWeight();
        this.requester = checkRequester(requester);
        this.department = requester.getDepartment();
        this.status = StatusTicket.OPEN;
    }

    public static Ticket create(String description, PriorityTicket priority, User requester) {
        return new Ticket(description, priority, requester);
    }

    @PostPersist
    public void generateFinalTitle() {
        if (this.id != null)
            this.title = "Ticket #" + this.id;
    }

    public void changeDescription(String newDescription) {
        ensureEditable();
        this.description = checkDescription(newDescription);
    }

    public void changePriority(PriorityTicket newPriority) {
        ensureEditable();
        if (this.priority.equals(newPriority)) return;
        this.priority = CommonValidation.required(newPriority, "ticket.field.priority");
        this.priorityWeight = newPriority.getWeight();
    }

    public void assignTechnician(User assignTechnician) {
        if (this.status != StatusTicket.OPEN && this.status != StatusTicket.IN_PROGRESS)
            throw new BusinessException(Code.TICKET_ALREADY_CLOSED, "ticket.error.ticket_already_closed");

        User technician = CommonValidation.required(assignTechnician, "ticket.field.technician");

        if (technician.getRole() != Role.ROLE_TECHNICIAN)
            throw new BusinessException(Code.INVALID_ASSIGNMENT, "ticket.error.invalid_technician");

        if(this.requester.getUuid().equals(technician.getUuid()))
            throw new BusinessException(Code.INVALID_ASSIGNMENT, "ticket.error.technician_cannot_be_requester");

        this.assignedTechnician = technician;

        if (StatusTicket.OPEN.equals(this.status))
            changeStatus(StatusTicket.IN_PROGRESS);
    }

    public void resolve() {
        changeStatus(StatusTicket.RESOLVED);
    }

    public void reopen() {
        changeStatus(StatusTicket.IN_PROGRESS);
    }

    public void close() {
        changeStatus(StatusTicket.CLOSED);
    }

    private void changeStatus(StatusTicket newStatus) {
        this.status.transitionTo(newStatus);
        this.status = newStatus;
    }

    private static String checkDescription(String description) {
        String validateDescription = CommonValidation.requiredText(description, "ticket.field.description", 10);
        if (!validateDescription.matches("^[\\s\\S]{10,200}$"))
            throw new BusinessException(Code.BUSINESS_RULE, "ticket.error.invalid_description");
        return validateDescription;
    }

    private User checkRequester(User requester) {
        return CommonValidation.required(requester, "ticket.field.requester");
    }

    private void ensureEditable() {
        if(status != StatusTicket.OPEN)
            throw new BusinessException(Code.TICKET_ALREADY_CLOSED, "ticket.error.ticket_already_closed");
    }

    @Override
    public String toString() {
        return "Ticket[" +
                "id=" + id +
                ", uuid=" + uuid +
                ", title='" + title + '\'' +
                ", priority=" + priority +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ']';
    }
}
