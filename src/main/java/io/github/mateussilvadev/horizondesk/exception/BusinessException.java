package io.github.mateussilvadev.horizondesk.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final Code code;
    private final String messageKey;
    private final Object[] args;

    public BusinessException(Code code, String messageKey, Object[] args) {
        super(messageKey);
        this.code = code;
        this.messageKey = messageKey;
        this.args = args;
    }

    public BusinessException(Code code, String messageKey) {
        this(code, messageKey, null);
    }
}
