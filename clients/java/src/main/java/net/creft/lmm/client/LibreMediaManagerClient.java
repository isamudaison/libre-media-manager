package net.creft.lmm.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class LibreMediaManagerClient {
    private static final String REQUEST_ID_HEADER = "X-Request-Id";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String baseUrl;
    private final Map<String, String> defaultHeaders;

    public LibreMediaManagerClient(String baseUrl) {
        this(HttpClient.newHttpClient(), defaultObjectMapper(), baseUrl, Map.of());
    }

    public LibreMediaManagerClient(String baseUrl, Map<String, String> defaultHeaders) {
        this(HttpClient.newHttpClient(), defaultObjectMapper(), baseUrl, defaultHeaders);
    }

    public LibreMediaManagerClient(
            HttpClient httpClient,
            ObjectMapper objectMapper,
            String baseUrl,
            Map<String, String> defaultHeaders
    ) {
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient is required");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper is required");
        this.baseUrl = normalizeBaseUrl(baseUrl);
        this.defaultHeaders = defaultHeaders == null ? Map.of() : Map.copyOf(defaultHeaders);
    }

    public MediaPage listMedia() throws IOException, InterruptedException {
        return listMedia(MediaListRequest.builder().build());
    }

    public MediaPage listMedia(MediaListRequest request) throws IOException, InterruptedException {
        MediaListRequest normalizedRequest = request == null ? MediaListRequest.builder().build() : request;
        return sendJson(requestBuilder("/media", normalizedRequest.toQueryParameters()).GET().build(), MediaPage.class);
    }

    public Media getMedia(String mediaId) throws IOException, InterruptedException {
        return sendJson(requestBuilder("/media/" + encodePathSegment(mediaId), Map.of()).GET().build(), Media.class);
    }

    public Media createMedia(CreateMediaRequest request) throws IOException, InterruptedException {
        return sendJson(withJsonBody(requestBuilder("/media", Map.of()), request).POST(bodyPublisher(request)).build(), Media.class);
    }

    public Media updateMedia(String mediaId, UpdateMediaRequest request) throws IOException, InterruptedException {
        return sendJson(
                withJsonBody(requestBuilder("/media/" + encodePathSegment(mediaId), Map.of()), request)
                        .PUT(bodyPublisher(request))
                        .build(),
                Media.class
        );
    }

    public void deleteMedia(String mediaId) throws IOException, InterruptedException {
        HttpRequest request = requestBuilder("/media/" + encodePathSegment(mediaId), Map.of())
                .DELETE()
                .build();
        sendNoContent(request);
    }

    public static ObjectMapper defaultObjectMapper() {
        return new ObjectMapper()
                .findAndRegisterModules()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private HttpRequest.Builder requestBuilder(String path, Map<String, String> query) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(buildUri(path, query))
                .header("Accept", "application/json");
        for (Map.Entry<String, String> entry : defaultHeaders.entrySet()) {
            builder.header(entry.getKey(), entry.getValue());
        }
        return builder;
    }

    private HttpRequest.Builder withJsonBody(HttpRequest.Builder builder, Object body) {
        builder.header("Content-Type", "application/json");
        return builder;
    }

    private HttpRequest.BodyPublisher bodyPublisher(Object body) {
        try {
            return HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body));
        } catch (JsonProcessingException exception) {
            throw new UncheckedIOException("Failed to serialize request body", exception);
        }
    }

    private URI buildUri(String path, Map<String, String> query) {
        StringBuilder uri = new StringBuilder(baseUrl).append(path);
        if (!query.isEmpty()) {
            uri.append("?");
            boolean first = true;
            for (Map.Entry<String, String> entry : query.entrySet()) {
                if (!first) {
                    uri.append("&");
                }
                uri.append(encodeQueryComponent(entry.getKey()))
                        .append("=")
                        .append(encodeQueryComponent(entry.getValue()));
                first = false;
            }
        }
        return URI.create(uri.toString());
    }

    private <T> T sendJson(HttpRequest request, Class<T> responseType) throws IOException, InterruptedException {
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return objectMapper.readValue(response.body(), responseType);
        }
        throw toApiException(response);
    }

    private void sendNoContent(HttpRequest request) throws IOException, InterruptedException {
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return;
        }
        throw toApiException(response);
    }

    private ApiException toApiException(HttpResponse<String> response) {
        String responseBody = response.body() == null ? "" : response.body();
        ApiError apiError = null;
        if (!responseBody.isBlank()) {
            try {
                apiError = objectMapper.readValue(responseBody, ApiError.class);
            } catch (IOException ignored) {
                apiError = null;
            }
        }
        String requestId = response.headers().firstValue(REQUEST_ID_HEADER).orElse(null);
        return new ApiException(response.statusCode(), requestId, apiError, responseBody);
    }

    private static String normalizeBaseUrl(String baseUrl) {
        Objects.requireNonNull(baseUrl, "baseUrl is required");
        if (baseUrl.isBlank()) {
            throw new IllegalArgumentException("baseUrl is required");
        }
        if (baseUrl.endsWith("/")) {
            return baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl;
    }

    private static String encodePathSegment(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("path segment is required");
        }
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private static String encodeQueryComponent(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
