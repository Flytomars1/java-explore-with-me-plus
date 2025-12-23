package ru.practicum.main.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // 404 - Not Found
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundException(NotFoundException e) {
        log.error("Not found: {}", e.getMessage());
        return buildApiError(
                HttpStatus.NOT_FOUND,
                "The required object was not found.",
                e.getMessage()
        );
    }

    // 409 - Already exists
    @ExceptionHandler(AlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleAlreadyExistsException(AlreadyExistsException e) {
        log.error("Already exists: {}", e.getMessage());
        return buildApiError(
                HttpStatus.CONFLICT,
                "Integrity constraint has been violated.",
                e.getMessage()
        );
    }

    // 409 - Conflict от БД
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDataIntegrityViolation(DataIntegrityViolationException e) {
        log.error("Data integrity violation: {}", e.getMessage());
        String message = "Integrity constraint violation";
        if (e.getMostSpecificCause() != null) {
            message = e.getMostSpecificCause().getMessage();
        }
        return buildApiError(
                HttpStatus.CONFLICT,
                "Integrity constraint has been violated.",
                message
        );
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflictException(ConflictException e) {
        log.error("Conflict: {}", e.getMessage());
        return buildApiError(
                HttpStatus.CONFLICT,
                "For the requested operation the conditions are not met.",
                e.getMessage()
        );
    }

    // 400 - Validation
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidationException(MethodArgumentNotValidException e) {
        log.error("Validation error: {}", e.getMessage());

        String errorMessage = "Incorrectly made request.";
        if (!e.getBindingResult().getFieldErrors().isEmpty()) {
            var fieldError = e.getBindingResult().getFieldErrors().get(0);
            errorMessage = String.format("Field: %s. Error: %s. Value: %s",
                    fieldError.getField(),
                    fieldError.getDefaultMessage(),
                    fieldError.getRejectedValue());
        }

        return buildApiError(
                HttpStatus.BAD_REQUEST,
                "Incorrectly made request.",
                errorMessage
        );
    }

    // 400 - Bad Request
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("Bad request: {}", e.getMessage());
        return buildApiError(
                HttpStatus.BAD_REQUEST,
                "Incorrectly made request.",
                e.getMessage()
        );
    }

    // 500
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleAllExceptions(Exception e) {
        log.error("Internal error: ", e);
        return buildApiError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal server error.",
                e.getMessage()
        );
    }

    private ApiError buildApiError(HttpStatus status, String reason, String message) {
        return ApiError.builder()
                .status(status)
                .reason(reason)
                .message(message)
                .timestamp(LocalDateTime.now().format(FORMATTER))
                .errors(Collections.emptyList())
                .build();
    }
}