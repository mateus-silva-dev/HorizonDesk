package io.github.mateussilvadev.horizondesk.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public record StandardError(
        Code code,
        int status,
        String message,
        Instant timestamp,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        Map<String, String> errors) {

    public static StandardError simple(Code code, int status, String message) {
        return new StandardError(code, status, message, Instant.now(), null);
    }

    public static StandardError validation(String message, BindingResult result) {
        Map<String, String> errors = result.getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> Objects.requireNonNullElse(fieldError.getDefaultMessage(), "Error message unavailable"),
                        (existing, replacement) -> existing
                ));

        return new StandardError(
                Code.VALIDATION_ERROR,
                HttpStatus.UNPROCESSABLE_CONTENT.value(),
                message,
                Instant.now(),
                errors
        );
    }
}
