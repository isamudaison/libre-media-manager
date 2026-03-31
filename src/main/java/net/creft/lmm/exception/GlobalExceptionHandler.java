package net.creft.lmm.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationFailure(MethodArgumentNotValidException exception) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            fieldErrors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
        }

        return buildResponse(HttpStatus.BAD_REQUEST, "Validation failed", "Validation failed", fieldErrors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleMalformedJson(HttpMessageNotReadableException exception) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Malformed JSON request body", "Malformed JSON request body", Map.of());
    }

    @ExceptionHandler(InvalidRequestParameterException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidRequestParameter(InvalidRequestParameterException exception) {
        return buildValidationResponse(Map.of(exception.getField(), exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleArgumentTypeMismatch(MethodArgumentTypeMismatchException exception) {
        return buildValidationResponse(Map.of(exception.getName(), exception.getName() + " must be a valid value"));
    }

    @ExceptionHandler(MediaNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleMediaNotFound(MediaNotFoundException exception) {
        return buildResponse(HttpStatus.NOT_FOUND, "Not Found", exception.getMessage(), Map.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException exception) {
        logger.info("Data integrity violation: {}", exception.getMostSpecificCause().getMessage());
        return buildResponse(HttpStatus.CONFLICT, "Conflict", "Request conflicts with existing data", Map.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnhandledException(Exception exception) {
        logger.error("Unhandled exception", exception);
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal server error",
                "An unexpected error occurred",
                Map.of()
        );
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(
            HttpStatus status,
            String error,
            String message,
            Map<String, String> fieldErrors
    ) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(error, message, fieldErrors));
    }

    private ResponseEntity<ApiErrorResponse> buildValidationResponse(Map<String, String> fieldErrors) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Validation failed", "Validation failed", fieldErrors);
    }
}
