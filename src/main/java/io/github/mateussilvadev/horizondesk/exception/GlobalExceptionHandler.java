package io.github.mateussilvadev.horizondesk.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<StandardError> handlerGeneric(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error occurred | Method: {} | Path: {} | Message: {}",
                request.getMethod(), request.getRequestURI(), ex.getMessage(), ex);

        String localizedMessage = messageSource.getMessage(
                "error.internal_server_error", null, "error.internal_server_error", LocaleContextHolder.getLocale());

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity
                .status(status)
                .body(StandardError.simple(Code.INTERNAL_SERVER_ERROR, status.value(), localizedMessage));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<StandardError> handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest request) {
        log.warn("Database constraint violation | Method: {} | Path: {}", request.getMethod(), request.getRequestURI());

        String rootMsg = ex.getMostSpecificCause().getMessage();

        String rootMsgLower = rootMsg != null ? rootMsg.toLowerCase() : "";

        if (rootMsg != null && (rootMsg.contains("uk6dotkott2kjsp8vw4d0m25fb7"))) {
            String localizedMessage = messageSource.getMessage(
                    "user_service.error.email_already_registered", null,
                    "user_service.error.email_already_registered", LocaleContextHolder.getLocale());
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(StandardError.simple(Code.EMAIL_ALREADY_REGISTERED, HttpStatus.CONFLICT.value(), localizedMessage));
        }
        else if (rootMsgLower.contains("ukj6cwks7xecs5jov19ro8ge3qk")) {
            String localizedMessage = messageSource.getMessage(
                    "department_service.error.department_already_registered", null,
                    "department_service.error.department_already_registered", LocaleContextHolder.getLocale());
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(StandardError.simple(Code.DEPARTMENT_ALREADY_REGISTERED, HttpStatus.CONFLICT.value(), localizedMessage));
        }

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(StandardError.simple(Code.BUSINESS_RULE, HttpStatus.CONFLICT.value(), "Erro de integridade de dados no sistema."));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<StandardError> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.warn("Malformed JSON syntax received | Method: {} | Path: {}", request.getMethod(), request.getRequestURI());

        String localizedMessage = messageSource.getMessage(
                "error.invalid_json", null, "error.invalid_json", LocaleContextHolder.getLocale());

        HttpStatus status = HttpStatus.BAD_REQUEST;

        return ResponseEntity
                .status(status)
                .body(StandardError.simple(Code.MALFORMED_JSON, status.value(), localizedMessage));
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<StandardError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.warn("Validation error occurred | Method: {} | Path: {}", request.getMethod(), request.getRequestURI());

        String localizedMessage = messageSource.getMessage(
                "error.validation_fields", null, "error.validation_fields", LocaleContextHolder.getLocale());

        return ResponseEntity
                .unprocessableContent()
                .body(StandardError.validation(localizedMessage, ex.getBindingResult()));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<StandardError> handleBusiness(BusinessException ex, HttpServletRequest request) {
        log.warn("Business error occurred | Method: {} | Path: {}", request.getMethod(), request.getRequestURI());

        Object[] args = ex.getArgs();

        if (args != null && args.length > 0 && args[0] instanceof String fieldKey)
            args[0] = messageSource.getMessage(fieldKey, null, fieldKey, LocaleContextHolder.getLocale());

        String localizedMessage = messageSource.getMessage(
                ex.getMessage(), null, ex.getMessage(), LocaleContextHolder.getLocale());

        HttpStatus status = HttpStatus.BAD_REQUEST;

        return ResponseEntity
                .status(status)
                .body(StandardError.simple(ex.getCode(), status.value(), localizedMessage));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<StandardError> handleEntityNotFound(EntityNotFoundException ex, HttpServletRequest request) {
        log.warn("Entity not found | Method: {} | Path: {}", request.getMethod(), request.getRequestURI());

        String localizedMessage = messageSource.getMessage(
                ex.getMessageKey(), null, ex.getMessageKey(), LocaleContextHolder.getLocale());

        HttpStatus status = HttpStatus.NOT_FOUND;

        return ResponseEntity
                .status(status)
                .body(StandardError.simple(Code.ENTITY_NOT_FOUND, status.value(), localizedMessage));
    }

}
