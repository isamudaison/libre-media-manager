# Libre Media Manager Taxonomy Specification

Status: Draft
Last updated: 2026-03-30

Parent spec: [project-spec.md](/home/isamudaison/Code/libre-media-manager/docs/project-spec.md)
Related spec: [media-domain-spec.md](/home/isamudaison/Code/libre-media-manager/docs/media-domain-spec.md)

## Purpose

This document defines how Libre Media Manager should represent classification and evaluative metadata.

It covers:

- categories
- tags
- ratings

These concepts are related, but they should not be treated as the same thing.

The goal of this spec is to prevent the backend from collapsing all taxonomy concerns into a single ad hoc string-list model.

## Goals

The taxonomy layer should:

- support a frontend-friendly browsing experience
- support filtering and discovery
- support both curated and flexible classification
- support structured ratings without confusing them with tags
- leave room for hierarchical media and richer future organization

## Non-Goals

This spec does not attempt to define:

- recommendation algorithms
- automatic tagging or AI enrichment
- moderation or multi-user permissions
- full external metadata synchronization

## Core Design Decisions

The recommended taxonomy design is:

1. `Category` is controlled and hierarchical.
2. `Tag` is free-form and flat.
3. `Rating` is structured and scheme-backed.
4. Media can be associated with many categories, many tags, and many ratings.
5. Taxonomy objects should be modeled as first-class related resources, not as anonymous strings embedded everywhere.

## Taxonomy Concepts

## Category

Categories are curated classification objects used for browsing, filtering, and navigation.

Examples:

- genre
- collection
- mood
- theme
- library section

Properties of categories:

- controlled vocabulary
- can be hierarchical
- usually managed by the library owner or system
- stable enough to power navigation UIs

Categories are appropriate when the frontend should be able to show:

- a sidebar or tree
- structured browse pages
- grouped views
- repeatable filters

## Tag

Tags are lightweight, free-form labels.

Examples:

- `space-opera`
- `rewatch`
- `favorites`
- `family-night`

Properties of tags:

- flat, not hierarchical
- flexible and user-friendly
- low-friction to create
- useful for ad hoc filtering and curation

Tags are appropriate when the library owner wants to add fast, informal labels without changing the core taxonomy tree.

## Rating

Ratings represent structured evaluations or classifications.

Examples:

- a personal score like `4.5/5`
- a content rating like `PG-13`
- an external score like `8.1/10`

Ratings are not tags and should not be stored as free-form labels.

They require:

- a defined scheme
- a constrained value format
- a clear meaning

## Recommended Taxonomy Model

## `Category`

Recommended fields:

- `categoryId`: UUID public identifier
- `key`: stable machine key or slug
- `displayName`: human-readable label
- `description`: optional explanation
- `categoryType`: enum
- `parentCategoryId`: nullable self-reference
- `sortOrder`: optional UI ordering field
- `active`: boolean
- `createdAt`
- `updatedAt`

Recommended `CategoryType` values:

- `GENRE`
- `COLLECTION`
- `THEME`
- `MOOD`
- `CUSTOM`

Recommended invariants:

- `key` is unique
- no category cycles
- parent must exist when present
- inactive categories are not assignable by default

Relationship to media:

- many-to-many

Recommended join model:

- `media_categories`

Suggested join fields:

- `media_id`
- `category_id`
- `assigned_at`

## `Tag`

Recommended fields:

- `tagId`: UUID public identifier
- `label`: display label
- `normalizedKey`: normalized lower-case key
- `createdAt`
- `updatedAt`

Recommended invariants:

- uniqueness enforced on `normalizedKey`
- trim whitespace
- collapse blank tags to invalid input
- no hierarchy

Relationship to media:

- many-to-many

Recommended join model:

- `media_tags`

Suggested join fields:

- `media_id`
- `tag_id`
- `assigned_at`

## `RatingScheme`

Ratings need a defined scheme so the system knows how to interpret a value.

Recommended fields:

- `ratingSchemeId`: UUID public identifier
- `key`: stable machine key
- `displayName`
- `ratingKind`: enum
- `scaleMax`: optional numeric upper bound
- `active`
- `createdAt`
- `updatedAt`

Recommended `RatingKind` values:

- `CONTENT`
- `SCORE`

Examples:

- `MPAA`, kind `CONTENT`
- `USER_STARS`, kind `SCORE`, `scaleMax = 5`
- `IMDB`, kind `SCORE`, `scaleMax = 10`

## `MediaRating`

This is the rating assignment attached to a media item.

Recommended fields:

- `mediaRatingId`: UUID public identifier
- `mediaId`
- `ratingSchemeId`
- `valueCode`: optional string value for code-based ratings such as `PG-13`
- `valueNumeric`: optional numeric value for scored ratings such as `4.5`
- `sourceType`: enum
- `sourceName`: optional source label
- `createdAt`
- `updatedAt`

Recommended `sourceType` values:

- `USER`
- `SYSTEM`
- `EXTERNAL`

Recommended invariants:

- exactly one of `valueCode` or `valueNumeric` should be populated
- `valueNumeric` must be within scheme bounds when the scheme is numeric
- only one active rating per `(media, scheme, sourceType, sourceName)` combination

Relationship to media:

- one-to-many from media

## Why Categories, Tags, and Ratings Must Stay Separate

They solve different problems:

- categories support structured navigation
- tags support quick ad hoc labeling
- ratings support evaluative or advisory meaning

If these are collapsed into one generic label system, the frontend loses semantics and the backend loses validation rules.

Examples of bad outcomes from collapsing them:

- `PG-13` treated like a search tag instead of a rating
- `Sci-Fi` treated like a free-form tag instead of a genre category
- personal score `4.5` stored as a meaningless label

## Frontend Implications

The frontend should be able to use taxonomy data in clearly distinct ways.

### Categories in the UI

Categories should power:

- structured browse navigation
- sidebar trees
- curated collections
- persistent filters

### Tags in the UI

Tags should power:

- chip-style labels
- ad hoc filters
- personal curation workflows

### Ratings in the UI

Ratings should power:

- badges like `PG-13`
- score displays like `4.5/5`
- sorting by personal or external score

## API Design Intent

Taxonomy resources should be addressable independently of media records.

Recommended resource groups:

- `/categories`
- `/tags`
- `/rating-schemes`
- `/media/{mediaId}/ratings`

Media responses should include taxonomy summaries, but media creation and update should not require nested full taxonomy object creation.

Recommended API pattern:

- create or manage taxonomy resources separately
- assign them to media by ID or key

Example media detail response fragment:

```json
{
  "mediaId": "c1c32f42-8919-4d6c-a0d8-9b4d42d2adbe",
  "title": "Arrival",
  "categories": [
    {
      "categoryId": "0f3d1be6-2d31-4f44-bf2d-2f7b4c5f8d91",
      "key": "genre/sci-fi",
      "displayName": "Sci-Fi",
      "categoryType": "GENRE"
    }
  ],
  "tags": [
    {
      "tagId": "63252865-0936-46bf-b2e1-f735b7864a0f",
      "label": "first-contact"
    }
  ],
  "ratings": [
    {
      "scheme": "USER_STARS",
      "valueNumeric": 4.5
    },
    {
      "scheme": "MPAA",
      "valueCode": "PG-13"
    }
  ]
}
```

## Normalization and Governance Rules

### Categories

- created deliberately, not casually
- can be ordered
- may be hidden or deactivated
- should be stable enough for bookmarks and saved filters

### Tags

- should be normalized for uniqueness
- may be merged later if duplicates arise
- should preserve a user-friendly display label

### Ratings

- must be validated against their scheme
- should preserve source context
- should not be reduced to plain strings if the frontend needs to sort or interpret them

## Suggested Persistence Shape

Recommended tables:

- `categories`
- `media_categories`
- `tags`
- `media_tags`
- `rating_schemes`
- `media_ratings`

This keeps taxonomy concerns normalized and prevents the `media` table from becoming a dumping ground for every classification concern.

## Rollout Recommendation

### Phase 1

Implement:

- categories
- tags

Reason:

- they add immediate browse/filter value
- they are simpler than full rating-scheme modeling

### Phase 2

Implement:

- rating schemes
- media ratings

Reason:

- ratings benefit from a more deliberate scheme model
- they should not be rushed into a weak string-only representation

### Phase 3

Implement:

- tag merge/alias support
- category tree management UX
- taxonomy analytics or recommendation support if needed

## Open Decisions

The following decisions should be confirmed before implementation:

1. Should `CategoryType` stay generic, or do we want separate category domains such as genre and collection modeled as separate resources?
2. Should tags be globally shared across the whole library, or isolated per user/library owner in the future?
3. Do we want only user ratings at first, or both user and external ratings in the first rating iteration?
4. Should a media item be able to have multiple categories of the same type, such as multiple genres?
5. Do we need aliases or synonyms for categories and tags in the first taxonomy release?

## Recommendation

Proceed with this split:

- categories = controlled, hierarchical
- tags = free-form, flat
- ratings = structured, scheme-backed

That is the cleanest model for a backend that is meant to power a real frontend and eventually support rich media browsing and playback workflows.
