package io.github.mateussilvadev.horizondesk;

import io.github.mateussilvadev.horizondesk.model.domain.Department;
import io.github.mateussilvadev.horizondesk.model.domain.Ticket;
import io.github.mateussilvadev.horizondesk.model.domain.User;
import io.github.mateussilvadev.horizondesk.model.enums.PriorityTicket;

public class Main {
    public static void main(String[] args) {

        Department department = Department.create("Sales Department");

        User user = User.create(
                "Mateus Silva", "mateus@hotizondesk.com", "123456789", department
        );

        Ticket ticket = Ticket.create("Monitor não liga", PriorityTicket.MEDIUM, user);

        System.out.printf("""
                Name user:      %s
                Department:     %s
                Ticket:         %s
                    Description:    %s
                    Priority:       %s
                    Status:         %s
                    
                    Ticket History: {
                    
                    }
                """, user.getName(), user.getDepartment().getName(), ticket.getTitle(),
                ticket.getDescription(), ticket.getPriority().name(), ticket.getStatus().name());
    }
}
