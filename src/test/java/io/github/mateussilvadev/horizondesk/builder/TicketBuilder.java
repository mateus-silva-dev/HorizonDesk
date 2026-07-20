package io.github.mateussilvadev.horizondesk.builder;

import io.github.mateussilvadev.horizondesk.model.domain.Department;
import io.github.mateussilvadev.horizondesk.model.domain.Ticket;
import io.github.mateussilvadev.horizondesk.model.domain.User;
import io.github.mateussilvadev.horizondesk.model.enums.PriorityTicket;
import io.github.mateussilvadev.horizondesk.model.enums.StatusTicket;
import lombok.Getter;
import net.datafaker.Faker;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Locale;
import java.util.UUID;

@Getter
public class TicketBuilder {

    private static final Faker faker = new Faker(Locale.of("pt","BR"));

    private Long id;
    private UUID uuid = UUID.randomUUID();
    private String title = "Ticket #PENDING";
    private String description = faker.lorem().characters(150);
    private PriorityTicket priority = PriorityTicket.LOW;
    private int priorityWeight = PriorityTicket.LOW.getWeight();
    private StatusTicket status = StatusTicket.OPEN;
    private User requester;
    private Department department;
    private User assignedTechnician;

    private TicketBuilder() {
        this.department = Department.create("TI TEST");
        this.requester = UserBuilder.anUser().withDepartment(this.department).build();
    }

    public static TicketBuilder anTicket() {
        return new TicketBuilder();
    }

    public TicketBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public TicketBuilder withUuid(UUID uuid) {
        this.uuid = uuid;
        return this;
    }

    public TicketBuilder withTitle(String title) {
        this.title = title;
        return this;
    }

    public TicketBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    public TicketBuilder withPriority(PriorityTicket priority) {
        this.priority = priority;
        this.priorityWeight = priority.getWeight();
        return this;
    }

    public TicketBuilder withStatus(StatusTicket status) {
        this.status = status;
        return this;
    }

    public TicketBuilder withRequester(User requester) {
        this.requester = requester;
        if (requester != null && requester.getDepartment() != null) {
            this.department = requester.getDepartment();
        }
        return this;
    }

    public TicketBuilder withDepartment(Department department) {
        this.department = department;
        return this;
    }

    public TicketBuilder withAssignedTechnician(User assignedTechnician) {
        this.assignedTechnician = assignedTechnician;
        return this;
    }

    public Ticket build() {
        Ticket ticket = Ticket.create(this.description, this.priority, this.requester);

        ReflectionTestUtils.setField(ticket, "id", this.id);
        ReflectionTestUtils.setField(ticket, "uuid", this.uuid);
        ReflectionTestUtils.setField(ticket, "title", this.title);
        ReflectionTestUtils.setField(ticket, "status", this.status);
        ReflectionTestUtils.setField(ticket, "priorityWeight", this.priorityWeight);
        ReflectionTestUtils.setField(ticket, "department", this.department);
        ReflectionTestUtils.setField(ticket, "assignedTechnician", this.assignedTechnician);

        return ticket;
    }

}
