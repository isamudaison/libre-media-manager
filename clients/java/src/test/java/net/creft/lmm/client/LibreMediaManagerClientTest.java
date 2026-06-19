package net.creft.lmm.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiPredicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LibreMediaManagerClientTest {
    private StubHttpClient httpClient;
    private LibreMediaManagerClient client;

    @BeforeEach
    void setUp() {
        httpClient = new StubHttpClient();
        client = new LibreMediaManagerClient(
                httpClient,
                LibreMediaManagerClient.defaultObjectMapper(),
                "http://127.0.0.1:8080",
                Map.of()
        );
    }

    @Test
    void listMedia_ParsesPageAndBuildsQueryString() throws Exception {
        httpClient.enqueue(200, Map.of("Content-Type", List.of("application/json")), """
                {
                  "items": [
                    {
                      "mediaId": "media-1",
                      "parentId": null,
                      "version": 3,
                      "title": "Arrival",
                      "originalTitle": "Story of Your Life",
                      "mediaType": "MOVIE",
                      "status": "ACTIVE",
                      "summary": "A linguist is recruited.",
                      "releaseDate": "2016-11-11",
                      "runtimeMinutes": 116,
                      "language": "en",
                      "createdAt": "2026-06-19T18:12:00Z",
                      "updatedAt": "2026-06-19T18:12:00Z",
                      "mediaFiles": [
                        {
                          "mediaFileId": "file-1",
                          "location": "/srv/media/arrival.mkv",
                          "label": "Main Feature",
                          "mimeType": "video/x-matroska",
                          "sizeBytes": 7340032000,
                          "durationSeconds": 6960,
                          "primaryFile": true,
                          "version": 0,
                          "createdAt": "2026-06-19T18:12:00Z",
                          "updatedAt": "2026-06-19T18:12:00Z",
                          "futureField": "ignored"
                        }
                      ],
                      "futureField": "ignored"
                    }
                  ],
                  "page": 1,
                  "size": 5,
                  "totalElements": 1,
                  "totalPages": 1
                }
                """);

        MediaPage page = client.listMedia(
                MediaListRequest.builder()
                        .title("arrival")
                        .mediaType(MediaType.MOVIE)
                        .page(1)
                        .size(5)
                        .build()
        );

        assertEquals("GET", httpClient.lastRequest().method());
        assertEquals("title=arrival&mediaType=MOVIE&page=1&size=5", httpClient.lastRequest().uri().getQuery());
        assertEquals(1, page.items().size());
        assertEquals("media-1", page.items().get(0).mediaId());
        assertEquals("file-1", page.items().get(0).mediaFiles().get(0).mediaFileId());
    }

    @Test
    void updateMedia_OnConflictThrowsApiException() throws Exception {
        httpClient.enqueue(409, Map.of(
                "Content-Type", List.of("application/json"),
                "X-Request-Id", List.of("req-42")
        ), """
                {
                  "error": "Conflict",
                  "message": "Media with id 'media-1' has version 4 but the request expected version 3",
                  "fieldErrors": {
                    "version": "version does not match the current media state"
                  }
                }
                """);

        ApiException exception = assertThrows(
                ApiException.class,
                () -> client.updateMedia(
                        "media-1",
                        new UpdateMediaRequest(
                                "Arrival",
                                3L,
                                null,
                                null,
                                MediaType.MOVIE,
                                null,
                                null,
                                null,
                                null,
                                null,
                                List.of()
                        )
                )
        );

        assertEquals("PUT", httpClient.lastRequest().method());
        assertEquals(409, exception.getStatusCode());
        assertEquals("req-42", exception.getRequestId());
        assertTrue(exception.getApiError().fieldErrors().containsKey("version"));
    }

    private static final class StubHttpClient extends HttpClient {
        private final Queue<StubHttpResponse> responses = new java.util.ArrayDeque<>();
        private HttpRequest lastRequest;

        void enqueue(int statusCode, Map<String, List<String>> headers, String body) {
            responses.add(new StubHttpResponse(statusCode, headers, body));
        }

        HttpRequest lastRequest() {
            return lastRequest;
        }

        @Override
        public Optional<CookieHandler> cookieHandler() {
            return Optional.empty();
        }

        @Override
        public Optional<Duration> connectTimeout() {
            return Optional.empty();
        }

        @Override
        public HttpClient.Redirect followRedirects() {
            return HttpClient.Redirect.NEVER;
        }

        @Override
        public Optional<ProxySelector> proxy() {
            return Optional.empty();
        }

        @Override
        public SSLContext sslContext() {
            return null;
        }

        @Override
        public SSLParameters sslParameters() {
            return new SSLParameters();
        }

        @Override
        public Optional<Executor> executor() {
            return Optional.empty();
        }

        @Override
        public HttpClient.Version version() {
            return HttpClient.Version.HTTP_1_1;
        }

        @Override
        public Optional<java.net.Authenticator> authenticator() {
            return Optional.empty();
        }

        @Override
        public <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) {
            this.lastRequest = request;
            StubHttpResponse response = responses.remove();
            @SuppressWarnings("unchecked")
            HttpResponse<T> typedResponse = (HttpResponse<T>) response;
            return typedResponse;
        }

        @Override
        public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) {
            throw new UnsupportedOperationException("Not needed in tests");
        }

        @Override
        public <T> CompletableFuture<HttpResponse<T>> sendAsync(
                HttpRequest request,
                HttpResponse.BodyHandler<T> responseBodyHandler,
                HttpResponse.PushPromiseHandler<T> pushPromiseHandler
        ) {
            throw new UnsupportedOperationException("Not needed in tests");
        }
    }

    private static final class StubHttpResponse implements HttpResponse<String> {
        private final int statusCode;
        private final HttpHeaders headers;
        private final String body;

        private StubHttpResponse(int statusCode, Map<String, List<String>> headers, String body) {
            this.statusCode = statusCode;
            this.headers = HttpHeaders.of(headers, new BiPredicate<>() {
                @Override
                public boolean test(String key, String value) {
                    return true;
                }
            });
            this.body = body;
        }

        @Override
        public int statusCode() {
            return statusCode;
        }

        @Override
        public HttpRequest request() {
            return null;
        }

        @Override
        public Optional<HttpResponse<String>> previousResponse() {
            return Optional.empty();
        }

        @Override
        public HttpHeaders headers() {
            return headers;
        }

        @Override
        public String body() {
            return body;
        }

        @Override
        public Optional<SSLSession> sslSession() {
            return Optional.empty();
        }

        @Override
        public URI uri() {
            return URI.create("http://127.0.0.1:8080");
        }

        @Override
        public HttpClient.Version version() {
            return HttpClient.Version.HTTP_1_1;
        }
    }
}
