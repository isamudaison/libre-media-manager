package net.creft.lmm.exception;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Schema(description = "Standard API error envelope")
public class ApiErrorResponse {
    @Schema(description = "Short error category", example = "Validation failed")
    private final String error;

    @Schema(description = "Human-readable error message", example = "Validation failed")
    private final String message;

    @Schema(description = "Field-level validation errors, when applicable")
    private final Map<String, String> fieldErrors;

    public ApiErrorResponse(String error, String message, Map<String, String> fieldErrors) {
        this.error = error;
        this.message = message;
        this.fieldErrors = Collections.unmodifiableMap(new LinkedHashMap<>(fieldErrors));
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
}
