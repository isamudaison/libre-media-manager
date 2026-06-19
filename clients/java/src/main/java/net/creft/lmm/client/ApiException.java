package net.creft.lmm.client;

public class ApiException extends RuntimeException {
    private final int statusCode;
    private final String requestId;
    private final ApiError apiError;
    private final String responseBody;

    public ApiException(int statusCode, String requestId, ApiError apiError, String responseBody) {
        super(buildMessage(statusCode, requestId, apiError, responseBody));
        this.statusCode = statusCode;
        this.requestId = requestId;
        this.apiError = apiError;
        this.responseBody = responseBody;
    }

    private static String buildMessage(int statusCode, String requestId, ApiError apiError, String responseBody) {
        if (apiError != null && apiError.message() != null && !apiError.message().isBlank()) {
            if (requestId == null || requestId.isBlank()) {
                return "HTTP " + statusCode + ": " + apiError.message();
            }
            return "HTTP " + statusCode + " [" + requestId + "]: " + apiError.message();
        }
        if (requestId == null || requestId.isBlank()) {
            return "HTTP " + statusCode + ": " + responseBody;
        }
        return "HTTP " + statusCode + " [" + requestId + "]: " + responseBody;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getRequestId() {
        return requestId;
    }

    public ApiError getApiError() {
        return apiError;
    }

    public String getResponseBody() {
        return responseBody;
    }
}
