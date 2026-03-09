package net.creft.lmm.exception;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class ApiErrorResponse {
    private final String error;
    private final String message;
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
