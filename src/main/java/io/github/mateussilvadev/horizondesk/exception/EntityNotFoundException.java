package io.github.mateussilvadev.horizondesk.exception;

import lombok.Getter;

@Getter
public class EntityNotFoundException extends RuntimeException {

    private final Code code = Code.ENTITY_NOT_FOUND;
    private final String messageKey;
    private final Object[] args;

    public EntityNotFoundException(String messageKey, Object[] args) {
        super(messageKey);
        this.messageKey = messageKey;
        this.args = args;
    }

    public EntityNotFoundException(String messageKey) {
        this(messageKey, null);
    }
}
