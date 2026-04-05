# Media Domain Model Specification

Status: Draft
Last updated: 2026-03-30

Parent spec: [project-spec.md](/home/isamudaison/Code/libre-media-manager/docs/project-spec.md)
Related spec: [taxonomy-spec.md](/home/isamudaison/Code/libre-media-manager/docs/taxonomy-spec.md)

## Purpose

This document defines the recommended next version of the domain model for Libre Media Manager.
It is intended to turn the current title-only `Media` record into a real catalog object with clear
business meaning, validation rules, persistence requirements, and API implications.

The current implementation stores only:

- `mediaId`
- `title`

The implementation has now started to introduce a `MediaFile` child collection with:

- `location`
- `label`
- `mimeType`
- `sizeBytes`
- `durationSeconds`
- `primaryFile`

That gives the API a useful playback-oriented file model, but broader file and playback-resource
modeling is still not fully specified.

That is enough for CRUD scaffolding, but not enough for a credible media-management domain.

## Recommended Scope

The recommended v1 scope is:

- model a media catalog record, not a file on disk
- support richer descriptive metadata
- support lifecycle state
- support filtering and sorting by key metadata fields
- keep the domain flat for now

The recommended v1 does not yet model:

- full file asset and playback resource modeling beyond attached `MediaFile` records
- ingest/transcoding workflows
- credits/cast/crew
- seasons, episodes, tracks, or child-item hierarchies
- provider sync jobs
- deduplication/merge workflows

Those are valid future concerns, but they should not be forced into the first serious domain revision.

## Core Design Decision

The current `Media` entity should remain the primary aggregate, but its meaning should be clarified:

- `Media` = one catalog entry representing a media work or collection-level item

Examples:

- one movie
- one TV series
- one book
- one audiobook
- one album
- one podcast
- one game

This keeps the model simple enough to implement cleanly while giving it real business value.

## Proposed Aggregate

### `Media`

Required fields:

- `mediaId`: immutable public identifier, UUID string
- `title`: primary display title
- `mediaType`: required enum
- `status`: required enum
- `createdAt`: server-managed timestamp
- `updatedAt`: server-managed timestamp

Optional fields:

- `originalTitle`: original or localized-source title
- `summary`: human-readable description
- `releaseDate`: canonical release date
- `runtimeMinutes`: duration in minutes for time-based media
- `language`: primary language code

## Proposed Enums

### `MediaType`

Recommended v1 values:

- `MOVIE`
- `SERIES`
- `BOOK`
- `AUDIOBOOK`
- `ALBUM`
- `PODCAST`
- `GAME`
- `OTHER`

Rationale:

- this is broad enough for a general media catalog
- it avoids premature hierarchy modeling like `SEASON`, `EPISODE`, or `TRACK`
- it can still be extended later without redesigning the whole aggregate

### `MediaStatus`

Recommended values:

- `DRAFT`
- `ACTIVE`
- `ARCHIVED`

Rationale:

- `DRAFT` supports incomplete metadata
- `ACTIVE` is the normal searchable/visible state
- `ARCHIVED` keeps records without hard-deleting them

## Field-Level Specification

### `mediaId`

- type: `String`
- format: UUID
- required: yes
- mutable: no
- uniqueness: globally unique
- API visibility: yes

### `title`

- type: `String`
- required: yes
- mutable: yes
- min length: `1`
- max length: `255`
- normalization: trim surrounding whitespace

### `originalTitle`

- type: `String`
- required: no
- mutable: yes
- max length: `255`
- normalization: trim; persist `null` if blank

### `mediaType`

- type: enum
- required: yes
- mutable: yes
- validation: must be one of the allowed enum values

### `status`

- type: enum
- required: yes
- mutable: yes
- default: `ACTIVE`

### `summary`

- type: `String`
- required: no
- mutable: yes
- max length: `4000`
- normalization: trim; persist `null` if blank

### `releaseDate`

- type: `LocalDate`
- required: no
- mutable: yes
- notes: can be in the future for upcoming releases

### `runtimeMinutes`

- type: `Integer`
- required: no
- mutable: yes
- validation: must be `> 0` when present
- notes: optional because not all media types have a meaningful runtime

### `language`

- type: `String`
- required: no
- mutable: yes
- max length: `16`
- suggested format: BCP 47 or simplified language tag such as `en`, `en-US`, `fr`
- normalization: trim; persist `null` if blank

### `createdAt`

- type: `Instant`
- required: yes
- mutable: no
- managed by: server

### `updatedAt`

- type: `Instant`
- required: yes
- mutable: no from API perspective
- managed by: server

## Recommended Persistence Shape

### `media` table

Recommended columns:

- `id bigint primary key`
- `media_id varchar(36) not null unique`
- `title varchar(255) not null`
- `original_title varchar(255) null`
- `media_type varchar(32) not null`
- `status varchar(32) not null`
- `summary varchar(4000) null`
- `release_date date null`
- `runtime_minutes integer null`
- `language varchar(16) null`
- `created_at timestamp with time zone not null`
- `updated_at timestamp with time zone not null`

Recommended indexes:

- unique index on `media_id`
- index on `media_type`
- index on `status`
- index on `release_date`
- index on `lower(title)` if title search becomes a real workload

Recommended constraints:

- `runtime_minutes > 0` when not null
- `media_type` restricted to enum values at application layer
- `status` restricted to enum values at application layer

## API Contract Changes

## Create Request

Current request:

```json
{
  "title": "Arrival"
}
```

Recommended request:

```json
{
  "title": "Arrival",
  "mediaType": "MOVIE",
  "originalTitle": null,
  "summary": "A linguist is recruited to communicate with extraterrestrial visitors.",
  "releaseDate": "2016-11-11",
  "runtimeMinutes": 116,
  "language": "en",
  "status": "ACTIVE"
}
```

Rules:

- `title` required
- `mediaType` required
- `status` optional in request, default `ACTIVE`
- all optional string fields should accept omission or `null`

## Update Request

Recommended near-term choice:

- keep `PUT /media/{mediaId}`
- treat it as a full update of mutable fields

Required on update:

- `title`
- `mediaType`

Optional on update:

- `originalTitle`
- `summary`
- `releaseDate`
- `runtimeMinutes`
- `language`
- `status`

Future option:

- add `PATCH /media/{mediaId}` later for partial updates

## Response Shape

Recommended item response:

```json
{
  "mediaId": "c1c32f42-8919-4d6c-a0d8-9b4d42d2adbe",
  "title": "Arrival",
  "originalTitle": null,
  "mediaType": "MOVIE",
  "status": "ACTIVE",
  "summary": "A linguist is recruited to communicate with extraterrestrial visitors.",
  "releaseDate": "2016-11-11",
  "runtimeMinutes": 116,
  "language": "en",
  "createdAt": "2026-03-29T23:10:00Z",
  "updatedAt": "2026-03-29T23:10:00Z"
}
```

## List Endpoint Evolution

Current filters:

- `title`

Recommended added filters:

- `mediaType`
- `status`
- `language`
- `releasedBefore`
- `releasedAfter`

Recommended sortable fields:

- `title`
- `mediaType`
- `status`
- `releaseDate`
- `createdAt`
- `updatedAt`

## Validation Rules

Recommended API validation:

- `title`: required, 1..255
- `originalTitle`: max 255
- `summary`: max 4000
- `runtimeMinutes`: positive integer
- `language`: max 16 and format-constrained if we decide to enforce BCP 47 strictly
- `mediaType`: required enum
- `status`: enum when provided

Recommended normalization rules:

- trim all string inputs
- convert blank optional strings to `null`
- never expose internal DB `id`
- keep `mediaId` immutable after creation

## Migration Strategy

Recommended migration sequence:

1. Add nullable optional columns plus non-null enum/timestamp columns with safe defaults.
2. Backfill existing rows:
   - `media_type = 'OTHER'`
   - `status = 'ACTIVE'`
   - `created_at = current_timestamp`
   - `updated_at = current_timestamp`
3. Update entity, DTOs, response models, and tests.
4. Expand list endpoint filtering and sorting.
5. Only after the new model is stable, consider adding child tables or external references.

Example migration shape:

```sql
alter table media add column original_title varchar(255);
alter table media add column media_type varchar(32) not null default 'OTHER';
alter table media add column status varchar(32) not null default 'ACTIVE';
alter table media add column summary varchar(4000);
alter table media add column release_date date;
alter table media add column runtime_minutes integer;
alter table media add column language varchar(16);
alter table media add column created_at timestamp not null default current_timestamp;
alter table media add column updated_at timestamp not null default current_timestamp;
create index idx_media_type on media (media_type);
create index idx_media_status on media (status);
create index idx_media_release_date on media (release_date);
```

## Implementation Phases

### Phase 1: Strong Core Metadata

Implement:

- new scalar fields
- enums
- timestamps
- DTO/response updates
- migration
- tests

Do not implement yet:

- tags
- external references
- hierarchies

### Phase 2: Classification and Provider References

Add:

- tags
- external IDs such as provider/source mappings

Possible tables:

- `media_tags`
- `media_external_reference`

### Phase 3: Hierarchies and Assets

Add only when product requirements are explicit:

- parent/child media relationships
- episodes/tracks/seasons
- file assets
- storage locations
- technical metadata

## Recommended First Implementation Slice

If implementation starts immediately, the cleanest first slice is:

1. add `mediaType`, `status`, `summary`, `releaseDate`, `runtimeMinutes`, `language`, `createdAt`, `updatedAt`
2. require `mediaType` on create/update
3. default `status` to `ACTIVE`
4. expose the new fields in item and list responses
5. add `mediaType` and `status` filters to `GET /media`

This gives the project a serious domain model without introducing relationship complexity too early.

## Decisions To Confirm Before Coding

The following choices should be confirmed before implementation:

1. Supported v1 media types:
   - Are `MOVIE`, `SERIES`, `BOOK`, `AUDIOBOOK`, `ALBUM`, `PODCAST`, `GAME`, `OTHER` sufficient?
2. Lifecycle policy:
   - Do we want `DRAFT`, or should the first version use only `ACTIVE` and `ARCHIVED`?
3. Update semantics:
   - Keep strict `PUT`, or introduce `PATCH` now?
4. Language format:
   - Do we want loose string validation or strict BCP 47 validation?
5. Near-term scope:
   - Are tags and external provider IDs needed in the first domain expansion, or can they wait for phase 2?

## Recommendation

Proceed with the Phase 1 design in this document.

It is the best tradeoff between:

- meaningful domain value
- implementation cost
- migration simplicity
- avoiding premature complexity
