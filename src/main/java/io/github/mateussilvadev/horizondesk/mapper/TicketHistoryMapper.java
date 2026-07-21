package io.github.mateussilvadev.horizondesk.mapper;

import io.github.mateussilvadev.horizondesk.dto.response.PageResponse;
import io.github.mateussilvadev.horizondesk.dto.response.TicketHistoryResponse;
import io.github.mateussilvadev.horizondesk.model.domain.Ticket;
import io.github.mateussilvadev.horizondesk.model.domain.TicketHistory;
import io.github.mateussilvadev.horizondesk.model.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class TicketHistoryMapper {

    Locale locale = LocaleContextHolder.getLocale();

    private final MessageSource messageSource;

    public TicketHistoryResponse toResponse(TicketHistory history) {
        if (history == null)
            return null;

        User actor = history.getActor();

        return new TicketHistoryResponse(
                history.getUuid(),
                //history.getType(),
                resolveMessage(history),
                //actor != null ? actor.getUuid() : null,
                actor != null ? actor.getName() : null);
                //history.getCreatedAt());
    }

    public List<TicketHistoryResponse> toResponseList(List<TicketHistory> histories) {
        if (histories == null || histories.isEmpty())
            return List.of();

        return histories.stream()
                .map(this::toResponse)
                .toList();
    }

    public PageResponse<TicketHistoryResponse> toPageResponse(Page<TicketHistory> page) {
        Page<TicketHistoryResponse> mappedPage = page.map(this::toResponse);
        return new PageResponse<>(
                mappedPage.getContent(),
                mappedPage.getNumber(),
                mappedPage.getSize(),
                mappedPage.getTotalElements(),
                mappedPage.getTotalPages(),
                mappedPage.isLast()
        );
    }

    private String resolveMessage(TicketHistory history) {
        return switch (history.getType()) {
            case OPENED -> getMessage("ticket.history.opened", actorName(history));

            case UPDATED -> getMessage("ticket.history.updated", actorName(history));

            case PRIORITY_CHANGED -> getMessage(
                    "ticket.history.priority-changed",
                    translatePriority(history.getOldValue(), locale),
                    translatePriority(history.getNewValue(), locale),
                    actorName(history));

            case TECHNICIAN_ASSIGNED -> {
                if (history.getActor() == null)
                    yield getMessage("ticket.history.technician_assigned", history.getNewValue());

                yield getMessage("ticket.history.technician_assigned_by", history.getNewValue(), history.getActor().getName());
            }

            case REOPENED -> getMessage(
                    "ticket.history.reopened",
                    actorName(history));

            case RESOLVED -> getMessage(
                    "ticket.history.resolved",
                    actorName(history));

            case CLOSED -> getMessage(
                    "ticket.history.closed",
                    actorName(history));
        };
    }

    private String translatePriority(String value, Locale locale) {
        if (value == null || value.isBlank())
            return "-";

        String messageKey = "ticket.priority." + value.toLowerCase(Locale.ROOT);
        return getMessage(messageKey, locale);
    }

    private String translateStatus(String value, Locale locale) {
        if (value == null || value.isBlank())
            return "-";

        String messageKey = "ticket.status." + value.toLowerCase(Locale.ROOT);
        return getMessage(messageKey, locale);
    }

    private String displayValue(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private String getMessage(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }

    private String actorName(TicketHistory history) {
        if (history.getActor() != null)
            return history.getActor().getName();
        return getMessage("ticket.history.actor.system");
    }
}
