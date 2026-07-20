package io.github.mateussilvadev.horizondesk.mapper;

import io.github.mateussilvadev.horizondesk.dto.request.TicketRequestDTOs;
import io.github.mateussilvadev.horizondesk.dto.response.PageResponse;
import io.github.mateussilvadev.horizondesk.dto.response.TicketResponseDTOs;
import io.github.mateussilvadev.horizondesk.model.domain.Ticket;
import io.github.mateussilvadev.horizondesk.model.domain.User;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;

public class TicketMapper {

    private static final ModelMapper MAPPER = new ModelMapper();
    static {
        MAPPER.createTypeMap(Ticket.class, TicketResponseDTOs.TicketResponse.class)
                .setConverter(context -> {
                    Ticket source = context.getSource();
                    if (source == null) return null;

                    String userName = (source.getRequester().getName() != null)
                            ? source.getRequester().getName() : null;

                    String departmentName = (source.getDepartment().getName() != null)
                            ? source.getDepartment().getName() : null;

                    return new TicketResponseDTOs.TicketResponse(
                            source.getUuid(),
                            source.getTitle(),
                            source.getDescription(),
                            source.getPriority(),
                            source.getStatus(),
                            userName,
                            departmentName,
                            source.getCreatedAt()
                    );
                });
    }

    private TicketMapper() { }

    public static Ticket toTicket(TicketRequestDTOs.TicketCreate dto, User requester) {
        return Ticket.create(
                dto.description(), dto.priorityTicket(), requester);
    }

    public static TicketResponseDTOs.TicketResponse toResponse(Ticket ticket) {
        if (ticket == null) return null;
        return MAPPER.map(ticket, TicketResponseDTOs.TicketResponse.class);
    }

    public static PageResponse<TicketResponseDTOs.TicketResponse> toTicketResponse(Page<Ticket> page) {
        Page<TicketResponseDTOs.TicketResponse> mappedPage = page.map(TicketMapper::toResponse);
        return new PageResponse<>(
                mappedPage.getContent(),
                mappedPage.getNumber(),
                mappedPage.getSize(),
                mappedPage.getTotalElements(),
                mappedPage.getTotalPages(),
                mappedPage.isLast()
        );
    }
}
