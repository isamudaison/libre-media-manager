# Agent TODOs: libre-media-manager

Last updated: 2026-03-29

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
| TODO-019 | P2 | Expand the domain model. | `docs/project-spec.md`, `docs/media-domain-spec.md`, `src/main/java/net/creft/lmm/model/Media.java`, `src/main/java/net/creft/lmm/dto/`, `src/main/java/net/creft/lmm/response/` | Media schema/API reflect agreed product fields. |

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

## Baseline Status

- Test command: `./mvnw test`
- Result at last scan: PASS (`38` tests, `0` failures)
