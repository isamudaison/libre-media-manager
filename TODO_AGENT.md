# Agent TODOs: libre-media-manager

Last updated: 2026-06-19

This file is intended for AI/human agents to quickly understand project intent and execute the highest-value next tasks.

## Project Intent

Build a Spring Boot backend service for managing media metadata, media relationships, and playback-oriented resource references for a local-first media library frontend.

## Prioritized TODO Backlog

| ID | Priority | Task | Key Files | Acceptance Criteria |
|---|---|---|---|---|
| TODO-001 | P0 | Add request validation for create/update payloads (`title` required, length bounds) using Bean Validation. | `src/main/java/net/creft/lmm/dto/CreateMediaRequest.java`, `src/main/java/net/creft/lmm/dto/UpdateMediaRequest.java`, `src/main/java/net/creft/lmm/controller/MediaController.java` | Invalid payloads return `400` with clear error messages; tests added for invalid JSON/body. |
| TODO-002 | P0 | Enforce DB integrity constraints for `Media` (`mediaId` unique + non-null, `title` non-null). | `src/main/java/net/creft/lmm/model/Media.java` | Duplicate `mediaId` is rejected at persistence layer; null/blank title cannot be saved. |
| TODO-003 | P1 | Introduce a service layer to separate HTTP concerns from business/persistence logic. (`done`) | `src/main/java/net/creft/lmm/controller/MediaController.java`, new `service` package | Controller becomes thin; business logic moved to service class with unit tests. |
| TODO-004 | P1 | Add centralized exception handling and a consistent error response schema. (`done`) | new `exception` package / `@ControllerAdvice` | Validation and runtime errors share a stable JSON error contract. |
| TODO-005 | P1 | Add `GET /media` list endpoint with pagination (and optional filtering). (`done`) | `src/main/java/net/creft/lmm/controller/MediaController.java`, `src/main/java/net/creft/lmm/repository/MediaRepository.java`, `src/main/java/net/creft/lmm/response/MediaPageResponse.java` | Can list media items with pageable params (`page`, `size`, optional sort/filter). |
| TODO-006 | P1 | Switch API responses from entities to explicit response DTOs. (`done`) | `src/main/java/net/creft/lmm/controller/MediaController.java`, `src/main/java/net/creft/lmm/response/MediaResponse.java`, `src/main/java/net/creft/lmm/response/MediaPageResponse.java` | API does not directly expose JPA entity internals. |
| TODO-007 | P2 | Remove or fully adopt `MediaResponse` wrapper (currently unused). (`done`) | `src/main/java/net/creft/lmm/response/MediaResponse.java` | No dead response models remain; response format decision is consistent. |
| TODO-008 | P2 | Fix logging semantics in `fetchMedia` so success/failure logs are accurate. (`done`) | `src/main/java/net/creft/lmm/service/MediaServiceImpl.java` | "Fetched" message appears only on actual hit; misses are logged clearly once. |
| TODO-009 | P1 | Expand controller tests for missing cases (GET not-found, validation failures, malformed payloads). (`done`) | `src/test/java/net/creft/lmm/controller/MediaControllerTest.java` | Tests cover negative paths and error contract behavior. |
| TODO-010 | P1 | Add integration tests with real JPA/H2 (not only repository-mocked MVC tests). (`done`) | `src/test/java/net/creft/lmm/integration/MediaIntegrationTest.java` | End-to-end CRUD flow verified with Spring context + H2. |
| TODO-011 | P2 | Add environment profiles and migration tooling (Flyway/Liquibase) for non-memory DB use. (`done`) | `src/main/resources/application.properties`, `src/main/resources/application-local.properties`, `src/main/resources/application-test.properties`, `src/main/resources/application-prod.properties`, `pom.xml`, `src/main/resources/db/migration/V1__create_media_table.sql` | Local/dev/prod config separation exists; schema is versioned and repeatable. |
| TODO-012 | P2 | Improve README with setup, run/test commands, endpoint docs, and examples. (`done`) | `README.md` | New contributor can run app/tests and call API without reading source code. |
| TODO-013 | P1 | Add CI automation. (`done`) | `.github/workflows/ci.yml` | CI runs `./mvnw test` on pull requests and default-branch pushes. |
| TODO-014 | P1 | Add Docker and Postgres runtime packaging. (`done`) | `Dockerfile`, `docker-compose.yml`, `.dockerignore` | App can run in a container and in a local app + Postgres stack. |
| TODO-015 | P1 | Harden API pagination and conflict handling. (`done`) | `src/main/java/net/creft/lmm/controller/MediaController.java`, `src/main/java/net/creft/lmm/exception/GlobalExceptionHandler.java`, `src/main/java/net/creft/lmm/exception/InvalidRequestParameterException.java` | Paging/sorting input is validated and persistence conflicts return `409`. |
| TODO-016 | P2 | Add OpenAPI documentation. (`done`) | `pom.xml`, `src/main/java/net/creft/lmm/controller/MediaController.java`, `README.md` | Developers can view generated API docs locally at `/v3/api-docs`. |
| TODO-017 | P2 | Clean test/runtime warnings. (`done`) | `pom.xml`, `src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker`, `src/test/resources/logback-test.xml`, `src/main/resources/application.properties`, `src/main/resources/application-test.properties` | Test runs are warning-free and less noisy. |
| TODO-018 | P2 | Add operational basics. (`done`) | `pom.xml`, `src/main/resources/application.properties`, `src/main/resources/application-test.properties`, `src/main/java/net/creft/lmm/config/RequestCorrelationFilter.java`, `src/test/java/net/creft/lmm/integration/MediaIntegrationTest.java` | Health checks and request correlation are available. |
| TODO-019 | P1 | Complete Phase 1 core media metadata on `Media`. (`done`) | `docs/media-domain-spec.md`, `src/main/java/net/creft/lmm/model/Media.java`, `src/main/resources/db/migration/`, `src/main/java/net/creft/lmm/dto/`, `src/main/java/net/creft/lmm/response/` | `mediaType` is required on create/update, `status` defaults to `ACTIVE`, richer scalar metadata/timestamps are persisted and exposed, and the migration safely backfills existing rows. |
| TODO-020 | P1 | Expand `GET /media` filtering and sorting for the richer metadata model. (`done`) | `src/main/java/net/creft/lmm/controller/MediaController.java`, `src/main/java/net/creft/lmm/repository/MediaRepository.java`, `src/main/java/net/creft/lmm/service/MediaServiceImpl.java`, `src/test/java/net/creft/lmm/integration/MediaIntegrationTest.java` | `GET /media` supports `parentId`, `mediaType`, `status`, `language`, `releasedBefore`, and `releasedAfter` filters plus the agreed sortable fields; invalid params return `400` with field errors. |
| TODO-021 | P2 | Align normalization and validation with the domain spec. (`done`) | `docs/media-domain-spec.md`, `src/main/java/net/creft/lmm/service/MediaServiceImpl.java`, `src/main/java/net/creft/lmm/exception/GlobalExceptionHandler.java`, `src/test/java/net/creft/lmm/service/MediaServiceImplTest.java`, `src/test/java/net/creft/lmm/controller/MediaControllerTest.java`, `src/test/java/net/creft/lmm/integration/MediaIntegrationTest.java` | `title` is trimmed before persist, blank optional strings collapse to `null`, numeric/enums validate cleanly, and tests cover edge-case normalization. |
| TODO-022 | P2 | Refresh docs and regression coverage for the richer media contract. (`done`) | `README.md`, `docs/media-domain-spec.md`, `TODO_AGENT.md`, `TODO_AGENT.json`, `src/test/java/net/creft/lmm/integration/MediaIntegrationTest.java` | README/OpenAPI/examples reflect the richer schema, `parentId`, and the current media types, and tests cover the updated published response shape. |
| TODO-023 | P1 | Add optimistic concurrency control for media updates. (`done`) | `src/main/java/net/creft/lmm/model/Media.java`, `src/main/resources/db/migration/V6__add_media_version.sql`, `src/main/java/net/creft/lmm/dto/UpdateMediaRequest.java`, `src/main/java/net/creft/lmm/controller/MediaController.java`, `src/main/java/net/creft/lmm/service/MediaServiceImpl.java`, `src/main/java/net/creft/lmm/exception/GlobalExceptionHandler.java` | Media responses expose a server-managed `version`, `PUT /media/{mediaId}` requires the latest version and returns `409` on stale writes, and regression coverage proves the conflict contract. |
| TODO-024 | P1 | Promote `MediaFile` to a standalone entity with loose media association. (`done`) | `src/main/java/net/creft/lmm/model/Media.java`, `src/main/java/net/creft/lmm/model/MediaFile.java`, `src/main/java/net/creft/lmm/repository/MediaFileRepository.java`, `src/main/resources/db/migration/V7__promote_media_file_to_entity.sql`, `src/main/java/net/creft/lmm/service/MediaServiceImpl.java`, `src/test/java/net/creft/lmm/integration/MediaIntegrationTest.java` | `MediaFile` persists independently with its own public ID, timestamps, and version; `Media` hydrates files by loose `mediaId` association instead of an embedded collection mapping; and an existing file can belong to at most one media item at a time. |
| TODO-025 | P1 | Scaffold maintainable client libraries for the current media API. (`done`) | `clients/spec/media-api-contract.json`, `clients/java/`, `clients/javascript/`, `clients/python/`, `README.md` | Java, React-friendly JavaScript, and Python clients expose the current `/media` endpoints with shared model naming, clear docs, and a centralized contract/update workflow that future agents can extend safely. |
| TODO-026 | P2 | Add client release and publishing automation. | `clients/`, `.github/workflows/`, future package metadata | Client libraries can be versioned and published independently, with CI verifying each package and update instructions covering release steps. |

## Completed Slice Design

This slice landed on 2026-04-04 and is now the baseline for the remaining Phase 1 follow-up work.

### In Scope

- keep `Media` as the primary catalog record while promoting `MediaFile` to a standalone entity
- add scalar metadata fields: `originalTitle`, `mediaType`, `status`, `summary`, `releaseDate`, `runtimeMinutes`, `language`, `createdAt`, `updatedAt`
- keep nested `mediaFiles` support on the current `/media` endpoints while storing files independently
- introduce `MediaType` and `MediaStatus` enums from the domain spec
- support lightweight `parentId` relationships and first-class `EPISODE` / `COLLECTION` records without deeper hierarchy semantics
- require `mediaType` on create/update and default `status` to `ACTIVE` when omitted
- expand `GET /media` filtering and sorting to use the new metadata
- normalize scalar inputs so `title` is trimmed and blank optional strings persist as `null`

### Out Of Scope

- categories, tags, ratings, or any taxonomy resource implementation
- external provider IDs or sync concerns
- deeper hierarchy/composite media modeling such as seasons or tracks beyond the current single `parentId` relationship and first-class `EPISODE` / `COLLECTION` media types
- `PATCH` semantics or partial-update design changes

### Contract Decisions

- `mediaId` remains immutable and server-generated
- `createdAt` and `updatedAt` are server-managed only
- `version` is a server-managed optimistic-lock token; `PUT /media/{mediaId}` must submit the latest value
- `mediaFiles` are standalone entities with their own `mediaFileId`, `createdAt`, `updatedAt`, and `version`
- `Media` exposes a hydrated ordered file view but no longer models file persistence as an embedded child collection
- a `MediaFile` can belong to at most one `Media` at a time, and nested `PUT /media/{mediaId}` requests may re-associate an existing file by `mediaFileId` plus file `version`
- `mediaFiles` remain request-ordered and keep the current single-primary-file rule
- taxonomy stays modeled as separate resources later; do not add ad hoc category/tag/rating strings to `Media`

### Suggested Implementation Order

1. Add `MediaType`/`MediaStatus` enums and a Flyway migration that expands the `media` table with safe defaults/backfill values plus supporting indexes.
2. Update entity, service, DTO, and response layers so create/update/read/list all expose the richer scalar contract while preserving the current `mediaFiles` behavior.
3. Expand `GET /media` filters/sort validation and repository queries for the new fields.
4. Extend repository, service, controller, and integration coverage; then refresh README/OpenAPI examples around the richer contract.

## Status Notes

- `TODO-001`: `done` (2026-03-09). Added Bean Validation constraints on create/update DTOs, `@Valid` request handling, and explicit `400` JSON responses for validation failures and malformed request bodies.
- `TODO-002`: `done` (2026-03-09). Added JPA column constraints for `mediaId` (`NOT NULL`, `UNIQUE`) and `title` (`NOT NULL`), with repository tests proving duplicate/null constraint enforcement.
- `TODO-003`: `done` (2026-03-09). Added `MediaService`/`MediaServiceImpl`, moved CRUD business and persistence flow out of `MediaController`, and added service unit tests for CRUD plus not-found paths.
- `TODO-004`: `done` (2026-03-09). Added centralized `@RestControllerAdvice` with a stable error response contract (`error`, `message`, `fieldErrors`) for validation, malformed JSON, not-found, and sanitized unhandled exceptions.
- `TODO-005`: `done` (2026-03-29). Added paginated `GET /media` with optional case-insensitive title filtering and explicit page response metadata.
- `TODO-006`: `done` (2026-03-29). Item and list endpoints now return explicit response DTOs instead of JPA entities.
- `TODO-007`: `done` (2026-03-29). Reused `MediaResponse` as the canonical flattened media DTO and eliminated the dead wrapper pattern.
- `TODO-008`: `done` (2026-03-29). Logging now occurs in `MediaServiceImpl` with accurate hit/miss semantics and no contradictory success log on misses.
- `TODO-009`: `done` (2026-03-29). Controller tests now cover GET not-found, malformed JSON, validation failures, sanitized unhandled exceptions, and paginated list behavior.
- `TODO-010`: `done` (2026-03-29). Added full-stack integration tests against H2 for CRUD flow and filtered pagination.
- `TODO-011`: `done` (2026-03-29). Added Flyway, an initial schema migration, local/test/prod profile files, and PostgreSQL runtime support for production.
- `TODO-012`: `done` (2026-03-29). Expanded README with setup, profiles, run/test commands, API examples, and response contract documentation.
- `TODO-013`: `done` (2026-03-29). Added GitHub Actions CI to run `./mvnw test` on pull requests and default-branch pushes.
- `TODO-014`: `done` (2026-03-29). Added a multi-stage Dockerfile, `.dockerignore`, and `docker-compose.yml` with Postgres for local containerized runs.
- `TODO-015`: `done` (2026-03-29). Hardened list endpoint input validation, capped page size, restricted sortable fields, and mapped persistence conflicts to HTTP `409`.
- `TODO-016`: `done` (2026-03-29). Added generated OpenAPI metadata for the current endpoints, documented the local docs endpoint at `/v3/api-docs`, and added integration coverage for the published contract. The API-only Springdoc starter is used because the Swagger UI starter was not compatible with the current Spring Boot 3.4.4 stack in this project.
- `TODO-017`: `done` (2026-03-29). Removed the Mockito self-attach warning by running tests with the Mockito Java agent, forced `debug=false` under Surefire so an ambient `DEBUG` environment variable cannot enable noisy Spring Boot debug output during tests, suppressed Spring test/bootstrap and Springdoc chatter with a test-only Logback config, disabled repository-test SQL echoing, and tightened the `test` profile logging. `./mvnw test` remains green and `./mvnw -q test` is silent on success.
- `TODO-018`: `done` (2026-03-29). Added Spring Boot Actuator health/info endpoints, enabled health probes, propagated or generated `X-Request-Id` values through a request-correlation filter, and included the request ID in the runtime log pattern. Integration tests cover `/actuator/health` plus request ID echo/generation behavior.
- `TODO-019`: `done` (2026-04-04). Implemented the Phase 1 core `Media` metadata slice from `docs/media-domain-spec.md`: added `MediaType`/`MediaStatus` enums, `originalTitle`, `summary`, `releaseDate`, `runtimeMinutes`, `language`, `createdAt`, and `updatedAt`; defaulted `status` to `ACTIVE`; required `mediaType` on create/update; added Flyway migration `V4__expand_media_table.sql`; normalized title/optional scalar handling in the service layer; and expanded repository/controller/integration coverage for the richer response contract.
- `TODO-020`: `done` (2026-04-05). Expanded `GET /media` to accept `parentId`, `mediaType`, `status`, `language`, `releasedBefore`, and `releasedAfter` filters on top of title search; added inclusive release-date range validation; expanded sortable fields to `mediaId`, `parentId`, `title`, `mediaType`, `status`, `releaseDate`, `createdAt`, and `updatedAt`; switched repository querying to JPA specifications; and added controller/service/integration coverage for combined filters plus invalid parameter contracts.
- `TODO-021`: `done` (2026-04-05). Tightened scalar-contract validation so request-body enum/date/type parse failures now return field-level `400` validation errors instead of a generic malformed-JSON response, added update-path normalization coverage to prove trimmed titles and blank optional strings still collapse correctly, and expanded controller/integration coverage for invalid body values plus immutable `mediaId` behavior through update flow assertions.
- `TODO-022`: `done` (2026-04-05). Refreshed README and `docs/media-domain-spec.md` around the shipped richer `Media` contract, first-class `parentId`, and `EPISODE` / `COLLECTION` support; updated the TODO tracker baseline; and expanded the OpenAPI integration assertions so the published schema exposes the richer filters, fields, and enum values.
- `TODO-023`: `done` (2026-06-19). Added a JPA/Flyway-backed `version` column on `Media`, exposed the version in read/create/list responses, required it on `PUT /media/{mediaId}`, mapped stale version mismatches and optimistic-locking races to HTTP `409`, and expanded controller/repository/service/integration coverage for the new conflict contract.
- `TODO-024`: `done` (2026-06-19). Promoted `MediaFile` from a collection-table value object to a standalone entity with its own public ID, timestamps, and version; added `MediaFileRepository` plus Flyway migration `V7__promote_media_file_to_entity.sql`; shifted `Media` to hydrate files through loose `mediaId` association instead of explicit JPA child persistence; and added integration coverage proving an existing file can be re-associated onto a different media item while belonging to at most one media item at a time.
- `TODO-025`: `done` (2026-06-19). Added the first client-library slice under `clients/`: a shared machine-readable media contract snapshot plus thin Java, fetch-based JavaScript/React, and Python clients for the shipped `/media` endpoints, with per-client docs and verification coverage for the Java and Python packages.

## Baseline Status

- Test command: `./mvnw test`
- Result at last scan: PASS (`76` tests, `0` failures) re-verified on 2026-06-19
