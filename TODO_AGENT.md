# Agent TODOs: libre-media-manager

Last updated: 2026-03-09

This file is intended for AI/human agents to quickly understand project intent and execute the highest-value next tasks.

## Project Intent

Build a Spring Boot backend service for managing media records with CRUD HTTP endpoints (`/media`), backed by Spring Data JPA.

## Prioritized TODO Backlog

| ID | Priority | Task | Key Files | Acceptance Criteria |
|---|---|---|---|---|
| TODO-001 | P0 | Add request validation for create/update payloads (`title` required, length bounds) using Bean Validation. | `src/main/java/net/creft/lmm/dto/CreateMediaRequest.java`, `src/main/java/net/creft/lmm/dto/UpdateMediaRequest.java`, `src/main/java/net/creft/lmm/controller/MediaController.java` | Invalid payloads return `400` with clear error messages; tests added for invalid JSON/body. |
| TODO-002 | P0 | Enforce DB integrity constraints for `Media` (`mediaId` unique + non-null, `title` non-null). | `src/main/java/net/creft/lmm/model/Media.java` | Duplicate `mediaId` is rejected at persistence layer; null/blank title cannot be saved. |
| TODO-003 | P1 | Introduce a service layer to separate HTTP concerns from business/persistence logic. (`done`) | `src/main/java/net/creft/lmm/controller/MediaController.java`, new `service` package | Controller becomes thin; business logic moved to service class with unit tests. |
| TODO-004 | P1 | Add centralized exception handling and a consistent error response schema. (`done`) | new `exception` package / `@ControllerAdvice` | Validation and runtime errors share a stable JSON error contract. |
| TODO-005 | P1 | Add `GET /media` list endpoint with pagination (and optional filtering). | `src/main/java/net/creft/lmm/controller/MediaController.java`, `src/main/java/net/creft/lmm/repository/MediaRepository.java` | Can list media items with pageable params (`page`, `size`, optional sort/filter). |
| TODO-006 | P1 | Switch API responses from entities to explicit response DTOs. | `src/main/java/net/creft/lmm/controller/MediaController.java`, new/updated response DTOs | API does not directly expose JPA entity internals. |
| TODO-007 | P2 | Remove or fully adopt `MediaResponse` wrapper (currently unused). | `src/main/java/net/creft/lmm/response/MediaResponse.java` | No dead response models remain; response format decision is consistent. |
| TODO-008 | P2 | Fix logging semantics in `fetchMedia` so success/failure logs are accurate. | `src/main/java/net/creft/lmm/controller/MediaController.java` | "Fetched" message appears only on actual hit; misses are logged clearly once. |
| TODO-009 | P1 | Expand controller tests for missing cases (GET not-found, validation failures, malformed payloads). | `src/test/java/net/creft/lmm/controller/MediaControllerTest.java` | Tests cover negative paths and error contract behavior. |
| TODO-010 | P1 | Add integration tests with real JPA/H2 (not only repository-mocked MVC tests). | new integration test class(es) under `src/test/java` | End-to-end CRUD flow verified with Spring context + H2. |
| TODO-011 | P2 | Add environment profiles and migration tooling (Flyway/Liquibase) for non-memory DB use. | `src/main/resources/application.properties`, `pom.xml`, new migration files | Local/dev/prod config separation exists; schema is versioned and repeatable. |
| TODO-012 | P2 | Improve README with setup, run/test commands, endpoint docs, and examples. | `README.md` | New contributor can run app/tests and call API without reading source code. |

## Status Notes

- `TODO-001`: `done` (2026-03-09). Added Bean Validation constraints on create/update DTOs, `@Valid` request handling, and explicit `400` JSON responses for validation failures and malformed request bodies.
- `TODO-002`: `done` (2026-03-09). Added JPA column constraints for `mediaId` (`NOT NULL`, `UNIQUE`) and `title` (`NOT NULL`), with repository tests proving duplicate/null constraint enforcement.
- `TODO-003`: `done` (2026-03-09). Added `MediaService`/`MediaServiceImpl`, moved CRUD business and persistence flow out of `MediaController`, and added service unit tests for CRUD plus not-found paths.
- `TODO-004`: `done` (2026-03-09). Added centralized `@RestControllerAdvice` with a stable error response contract (`error`, `message`, `fieldErrors`) for validation, malformed JSON, not-found, and sanitized unhandled exceptions.

## Baseline Status

- Test command: `./mvnw test`
- Result at last scan: PASS (`22` tests, `0` failures)
