# libre-media-manager

Spring Boot backend service for managing media records over HTTP.

## Requirements

- Java 17+
- Maven wrapper (`./mvnw`)

## Run

Start the app with the default `local` profile:

```bash
./mvnw spring-boot:run
```

The local profile uses an in-memory H2 database and enables the H2 console at `/h2-console`.

## Profiles

- `local`: in-memory H2 for local development, SQL logging enabled
- `test`: in-memory H2 for automated tests
- `prod`: external database via environment variables

`application.properties` sets `local` as the default profile. To run a different profile:

```bash
SPRING_PROFILES_ACTIVE=prod ./mvnw spring-boot:run
```

## Database

Schema changes are managed with Flyway migrations under `src/main/resources/db/migration`.

## Test

```bash
./mvnw test
```

## CI

A GitHub Actions workflow is defined at `.github/workflows/ci.yml` and runs `./mvnw test` on pushes to `main`/`master` and on pull requests.

## Docker

Build and run the application image:

```bash
docker build -t libre-media-manager .
docker run --rm -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DATABASE_URL=jdbc:postgresql://host.docker.internal:5432/libre_media_manager \
  -e DATABASE_USERNAME=libre_media_manager \
  -e DATABASE_PASSWORD=libre_media_manager \
  libre-media-manager
```

For a local app + Postgres stack:

```bash
docker compose up --build
```

## API

Generated OpenAPI JSON is available locally at `/v3/api-docs`.

Operational endpoints are exposed under `/actuator`:

- `/actuator/health`
- `/actuator/info`

Clients can supply an `X-Request-Id` header on any request. If present, the app echoes it back in the response; if absent, the app generates one. Runtime logs include the active request ID as `requestId:<value>` so request flows can be correlated across log lines.

`mediaFiles` are stored in request order. At most one entry may set `primaryFile` to `true`.

### Create media

```bash
curl -X POST http://localhost:8080/media \
  -H 'Content-Type: application/json' \
  -d '{
    "title":"Arrival",
    "mediaFiles":[
      {
        "location":"/srv/media/arrival.mkv",
        "label":"Main Feature",
        "mimeType":"video/x-matroska",
        "sizeBytes":7340032000,
        "durationSeconds":6960,
        "primaryFile":true
      }
    ]
  }'
```

### List media

Supports pagination and optional case-insensitive title filtering.

- `page` must be `>= 0`
- `size` must be between `1` and `100`
- `sort` must be one of `title` or `mediaId`
- `direction` must be `asc` or `desc`

```bash
curl 'http://localhost:8080/media?page=0&size=10&sort=title&direction=asc&title=ring'
```

### Get media

```bash
curl http://localhost:8080/media/<mediaId>
```

With request correlation:

```bash
curl -H 'X-Request-Id: req-42' http://localhost:8080/media/<mediaId>
```

### Update media

```bash
curl -X PUT http://localhost:8080/media/<mediaId> \
  -H 'Content-Type: application/json' \
  -d '{
    "title":"Arrival (Updated)",
    "mediaFiles":[
      {
        "location":"/srv/media/arrival-4k.mkv",
        "label":"4K Remux",
        "mimeType":"video/x-matroska",
        "sizeBytes":18340032000,
        "durationSeconds":6960,
        "primaryFile":true
      },
      {
        "location":"/srv/media/arrival-commentary.mkv",
        "label":"Commentary Track",
        "mimeType":"audio/flac",
        "sizeBytes":834003200,
        "durationSeconds":7020,
        "primaryFile":false
      }
    ]
  }'
```

### Delete media

```bash
curl -X DELETE http://localhost:8080/media/<mediaId>
```

### Health

```bash
curl http://localhost:8080/actuator/health
```

## Response shape

Single media responses:

```json
{
  "mediaId": "generated-id",
  "title": "Arrival",
  "mediaFiles": [
    {
      "location": "/srv/media/arrival.mkv",
      "label": "Main Feature",
      "mimeType": "video/x-matroska",
      "sizeBytes": 7340032000,
      "durationSeconds": 6960,
      "primaryFile": true
    }
  ]
}
```

Paginated list responses:

```json
{
  "items": [
    {
      "mediaId": "generated-id",
      "title": "Arrival",
      "mediaFiles": [
        {
          "location": "/srv/media/arrival.mkv",
          "label": "Main Feature",
          "mimeType": "video/x-matroska",
          "sizeBytes": 7340032000,
          "durationSeconds": 6960,
          "primaryFile": true
        }
      ]
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 1,
  "totalPages": 1
}
```

Validation and not-found failures use a stable JSON error contract with `error`, `message`, and `fieldErrors`.

Persistence conflicts are returned as HTTP `409 Conflict` with the same response envelope.
