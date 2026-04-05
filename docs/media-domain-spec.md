# Media Domain Model Specification

Status: Current implementation-aligned draft
Last updated: 2026-04-05

Parent spec: [project-spec.md](/home/isamudaison/Code/libre-media-manager/docs/project-spec.md)
Related spec: [taxonomy-spec.md](/home/isamudaison/Code/libre-media-manager/docs/taxonomy-spec.md)

## Purpose

This document defines the current Phase 1 domain model for Libre Media Manager.
It documents the richer `Media` contract that has now landed in the codebase and the near-term
constraints that shape how catalog records, playback files, and lightweight parent-child
relationships work today.

The implementation now stores:

- `mediaId`
- `parentId`
- `title`
- `originalTitle`
- `mediaType`
- `status`
- `summary`
- `releaseDate`
- `runtimeMinutes`
- `language`
- `createdAt`
- `updatedAt`

It also supports an ordered `MediaFile` child collection with:

- `location`
- `label`
- `mimeType`
- `sizeBytes`
- `durationSeconds`
- `primaryFile`

That gives the API a credible metadata-plus-playback baseline while leaving deeper asset and
taxonomy modeling for later phases.

## Current Scope

The current Phase 1 scope is:

- model a media catalog record, not a file on disk
- support richer descriptive metadata
- support lifecycle state
- support lightweight parent-child grouping through `parentId`
- support filtering and sorting by key metadata fields
- keep hierarchy semantics intentionally shallow for now

The current implementation does not yet model:

- full file asset and playback resource modeling beyond attached `MediaFile` records
- ingest/transcoding workflows
- credits/cast/crew
- season-specific metadata, track-specific metadata, or multi-level hierarchy semantics
- provider sync jobs
- deduplication/merge workflows

Those are valid future concerns, but they should not be forced into the first serious domain revision.

## Core Design Decision

The current `Media` entity should remain the primary aggregate, but its meaning should be clarified:

- `Media` = one catalog entry representing a media work or collection-level item

Examples:

- one movie
- one episode
- one TV series
- one collection
- one book
- one audiobook
- one album
- one podcast
- one game

This keeps the model simple enough to implement cleanly while giving it real business value.

## Current Aggregate

### `Media`

Required fields:

- `mediaId`: immutable public identifier, UUID string
- `title`: primary display title
- `mediaType`: required enum
- `status`: required enum
- `createdAt`: server-managed timestamp
- `updatedAt`: server-managed timestamp

Optional fields:

- `parentId`: optional parent `mediaId` for lightweight grouping
- `originalTitle`: original or localized-source title
- `summary`: human-readable description
- `releaseDate`: canonical release date
- `runtimeMinutes`: duration in minutes for time-based media
- `language`: primary language code

## Current Enums

### `MediaType`

Current values:

- `MOVIE`
- `EPISODE`
- `SERIES`
- `COLLECTION`
- `BOOK`
- `AUDIOBOOK`
- `ALBUM`
- `PODCAST`
- `GAME`
- `OTHER`

Rationale:

- this is broad enough for a general media catalog
- it supports lightweight collection and episode records without forcing deeper season/track design
- it can still be extended later without redesigning the whole aggregate

### `MediaStatus`

Current values:

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

### `parentId`

- type: `String`
- format: UUID
- required: no
- mutable: yes
- validation: when provided, must reference an existing `mediaId`
- cycle policy: cannot reference the same item or create a parent cycle
- delete behavior: clears to `null` if the parent item is deleted

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

## Current Persistence Shape

### `media` table

Current columns:

- `id bigint primary key`
- `media_id varchar(36) not null unique`
- `parent_id varchar(36) null`
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

Current indexes:

- unique index on `media_id`
- index on `parent_id`
- index on `media_type`
- index on `status`
- index on `release_date`
- index on `lower(title)` if title search becomes a real workload

Current constraints:

- `runtime_minutes > 0` when not null
- `parent_id` references `media(media_id)` with `on delete set null`
- `media_type` restricted to enum values at application layer
- `status` restricted to enum values at application layer

### `media_file` collection table

Current shape:

- owned by `Media`
- stored in request order using `file_order`
- no Java back-reference from `MediaFile` to `Media`
- at most one entry may set `primaryFile = true`

## Current API Contract

## Create Request

Current request:

```json
{
  "title": "Arrival",
  "parentId": "91b70c8f-4d1b-4c15-bfb1-66b063d6d363",
  "mediaType": "MOVIE",
  "originalTitle": null,
  "status": "ACTIVE",
  "summary": "A linguist is recruited to communicate with extraterrestrial visitors.",
  "releaseDate": "2016-11-11",
  "runtimeMinutes": 116,
  "language": "en",
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

Rules:

- `title` required
- `mediaType` required
- `parentId` optional
- `parentId` must reference an existing `mediaId` when present
- `status` optional in request, default `ACTIVE`
- all optional string fields should accept omission or `null`

## Update Request

Current behavior:

- keep `PUT /media/{mediaId}`
- treat it as a full update of mutable fields

Required on update:

- `title`
- `mediaType`

Optional on update:

- `parentId`
- `originalTitle`
- `summary`
- `releaseDate`
- `runtimeMinutes`
- `language`
- `status`
- `mediaFiles`

Future option:

- add `PATCH /media/{mediaId}` later for partial updates

## Response Shape

Current item response:

```json
{
  "mediaId": "c1c32f42-8919-4d6c-a0d8-9b4d42d2adbe",
  "parentId": "91b70c8f-4d1b-4c15-bfb1-66b063d6d363",
  "title": "Arrival",
  "originalTitle": null,
  "mediaType": "MOVIE",
  "status": "ACTIVE",
  "summary": "A linguist is recruited to communicate with extraterrestrial visitors.",
  "releaseDate": "2016-11-11",
  "runtimeMinutes": 116,
  "language": "en",
  "createdAt": "2026-03-29T23:10:00Z",
  "updatedAt": "2026-03-29T23:10:00Z",
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

## List Endpoint

Current filters:

- `title`
- `parentId`
- `mediaType`
- `status`
- `language`
- `releasedBefore`
- `releasedAfter`

Current sortable fields:
- `mediaId`
- `parentId`
- `title`
- `mediaType`
- `status`
- `releaseDate`
- `createdAt`
- `updatedAt`

## Validation Rules

Current API validation:

- `title`: required, 1..255
- `parentId`: max 36, must reference an existing `mediaId`, cannot self-reference, cannot create a cycle
- `originalTitle`: max 255
- `summary`: max 4000
- `runtimeMinutes`: positive integer
- `language`: max 16 and format-constrained if we decide to enforce BCP 47 strictly
- `mediaType`: required enum
- `status`: enum when provided

Current normalization rules:

- trim all string inputs
- convert blank optional strings to `null`
- never expose internal DB `id`
- keep `mediaId` immutable after creation
- keep `createdAt` and `updatedAt` server-managed
- keep `mediaFiles` request-ordered

## Migration Strategy

Implemented migration sequence:

1. Add nullable optional columns plus non-null enum/timestamp columns with safe defaults.
2. Backfill existing rows:
   - `media_type = 'OTHER'`
   - `status = 'ACTIVE'`
   - `created_at = current_timestamp`
   - `updated_at = current_timestamp`
3. Add `parent_id` as a nullable self-reference to `media(media_id)` with `on delete set null`.
4. Update entity, DTOs, response models, and tests.
5. Expand list endpoint filtering and sorting.
6. Only after the new model is stable, consider deeper hierarchy semantics or external references.

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
alter table media add column parent_id varchar(36);
alter table media add constraint fk_media_parent
    foreign key (parent_id) references media(media_id) on delete set null;
create index idx_media_parent_id on media (parent_id);
create index idx_media_type on media (media_type);
create index idx_media_status on media (status);
create index idx_media_release_date on media (release_date);
```

## Future Expansion Areas

### Phase 2: Classification and Provider References

Add:

- tags
- external IDs such as provider/source mappings

Possible tables:

- `media_tags`
- `media_external_reference`

### Phase 3: Deeper Hierarchies and Assets

Add only when product requirements are explicit:

- seasons/tracks and deeper relationship semantics beyond a single `parentId`
- file assets
- storage locations
- technical metadata

## Current Implementation Summary

The current implementation now includes:

1. scalar metadata fields plus `createdAt` and `updatedAt`
2. required `mediaType` on create and update
3. default `status = ACTIVE` when omitted
4. ordered `mediaFiles` with a single-primary-file rule
5. optional `parentId` with existence, self-reference, and cycle validation
6. `ON DELETE SET NULL` parent cleanup behavior
7. list filtering by `title`, `parentId`, `mediaType`, `status`, `language`, `releasedBefore`, and `releasedAfter`
8. first-class `COLLECTION` and `EPISODE` media types

## Known Follow-Up Areas

The next questions are now about breadth, not the baseline media contract:

1. taxonomy resources such as categories, tags, and ratings
2. external provider and sync identifiers
3. deeper hierarchy semantics such as seasons or track ordering
4. stricter language-tag validation
5. whether `PATCH` should supplement the current strict `PUT`

## Summary

This document now describes the shipped Phase 1 baseline.

It keeps the model useful without overcommitting to deeper hierarchy semantics, provider sync,
or taxonomy design before those requirements are fully specified.
