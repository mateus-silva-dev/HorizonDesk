package io.github.mateussilvadev.horizondesk.repository;

import io.github.mateussilvadev.horizondesk.dto.request.TicketFilter;
import io.github.mateussilvadev.horizondesk.model.domain.Ticket;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.criteria.JoinType.INNER;

public final class TicketSpecifications {

    private TicketSpecifications() { }

    public static Specification<Ticket> withFilter(TicketFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.status() != null) {
                predicates.add(cb.equal(root.get("status"), filter.status()));
            }
            if (filter.priority() != null) {
                predicates.add(cb.equal(root.get("priority"), filter.priority()));
            }
            if (filter.departmentUuid() != null) {
                predicates.add(cb.equal(root.get("department").get("uuid"), filter.departmentUuid()));
            }
            if (filter.requesterUuid() != null) {
                predicates.add(cb.equal(root.get("requester").get("uuid"), filter.requesterUuid()));
            }
            if (filter.technicianUuid() != null) {
                predicates.add(cb.equal(root.get("technician").get("uuid"), filter.technicianUuid()));
            }

            if (Long.class != query.getResultType()) {
                root.fetch("requester", INNER);
                root.fetch("department", INNER);
            }
            return cb.and(predicates.toArray(new Predicate[0]));

        };
    }
}
