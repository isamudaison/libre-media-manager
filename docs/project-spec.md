# Libre Media Manager Project Specification

Status: Draft
Last updated: 2026-03-29

## Purpose

This document defines the high-level goal of Libre Media Manager and the intended role of the backend service.

It sits above the domain model work in [media-domain-spec.md](/home/isamudaison/Code/libre-media-manager/docs/media-domain-spec.md) and answers:

- what this project is for
- who it serves
- what problems the backend is expected to solve
- what kinds of data and relationships the system should ultimately support
- where the responsibility line sits between backend and frontend

## Project Goal

Libre Media Manager is a backend service for managing media metadata and related playback-oriented data for a local media library.

The service should become the source of truth for:

- descriptive media metadata
- structural and hierarchical media relationships
- classification and organizational metadata
- playback-relevant resource references

The ultimate goal is to power a frontend that can:

- browse a media library in a user-friendly way
- search, filter, and organize media
- display rich metadata and artwork
- initiate playback of local-network media resources

The primary deployment target is a local user or household environment, especially resources reachable on a LAN.

## Product Vision

The long-term vision is a local-first media catalog and playback backend that allows a person or household to manage their own media library without depending on a hosted SaaS platform.

In practical terms, the system should let a frontend behave like a polished media browser and launcher, while the backend remains the authoritative system for metadata, relationships, and playback descriptors.

## Problem Statement

Local media collections are often fragmented across:

- file shares
- removable storage
- manually maintained folders
- inconsistent filenames
- incomplete or duplicated metadata

Frontend applications can display and play media well, but they need a stable backend to provide structured data.

Libre Media Manager exists to solve that gap by providing a backend that can model media as meaningful library objects rather than as loose files alone.

## Primary Users

### Library owner

The person who manages the library and wants to:

- maintain metadata
- categorize and organize content
- correct titles and artwork
- keep playback resources attached to the right media item

### Viewer

The person using a frontend that reads the backend and wants to:

- browse content comfortably
- view rich details
- start playback quickly

### Frontend client

The application consuming this backend and expecting:

- stable APIs
- predictable metadata
- clear playback descriptors
- enough structure to render a good media experience

## Deployment Assumptions

The default target environment is:

- self-hosted
- local-first
- single user or small household
- private LAN or similarly trusted network

The system should assume that:

- media resources are often stored on local disks, NAS devices, or LAN-accessible shares
- the frontend and backend are usually on the same local network
- direct playback is preferred where possible

The system should not assume:

- public internet exposure
- large-scale multi-tenant usage
- cloud-first storage

## System Responsibilities

The backend is responsible for being the authoritative store and API for the media library.

It should eventually manage:

- media records
- media-to-file relationships
- media classifications
- ratings
- categories
- tags
- artwork references
- playback references
- hierarchical relationships between media objects where needed

Concretely, the backend should provide:

- read and write APIs for metadata
- filtering, sorting, and search-oriented retrieval
- stable identifiers for media entities
- structured relationships between media and related records
- enough playback metadata for a frontend to safely choose and launch media

## Backend Data Scope

The backend should support both first-class and composite media properties.

### First-class properties

These are direct scalar properties of a media item, such as:

- title
- original title
- summary
- release date
- media type
- language
- runtime
- status

### Composite or related properties

These are child objects, collections, or linked records associated with a media item, such as:

- `mediaFiles`
- `ratings`
- `categories`
- `tags`
- `artwork`
- `externalReferences`
- `relationships` to other media items

This is important because the project is not just a title database.
It should grow into a media metadata service that can represent a real library.

## Hierarchical and Composite Modeling Intent

The system should eventually support media that is not purely flat.

Examples:

- a series containing seasons and episodes
- an album containing tracks
- a collection containing multiple media entries
- a movie or episode linked to multiple media files
- a media item with multiple ratings or classifications

This means the design should preserve room for:

- one-to-many relationships
- many-to-many relationships where appropriate
- parent-child media relationships
- file-level metadata separated from media-level metadata

The backend should avoid collapsing all concerns into a single oversized `Media` table.

## Playback Intent

Playback is part of the product goal, but the backend’s role is to enable playback rather than to become a full media player.

The backend should eventually expose enough information for a frontend to play content, such as:

- the available media files for an item
- the preferred playback target
- MIME type or container information
- subtitles or alternate tracks when supported
- local or streamable resource references

The frontend should remain responsible for:

- rendering the playback UI
- choosing how to start playback
- controlling the player
- presenting playback errors to the user

The expected first target is direct or near-direct playback on a trusted local network, not internet-scale streaming infrastructure.

## Frontend Relationship

The frontend is a first-class consumer of this service.

The backend should therefore be designed so a frontend can:

- render overview pages
- render detail pages
- display categorized and filtered views
- show poster/artwork and metadata
- navigate relationships between items
- choose a playable file or stream target

This implies the backend API should be:

- stable
- frontend-friendly
- explicit
- capable of returning both summary and detail-oriented shapes

## Architecture Principles

The project should follow these high-level principles:

### API-first

The backend should expose clean contracts that a frontend can depend on without knowing internal storage details.

### Backend as source of truth

The backend should own canonical metadata, identifiers, and relationships.

### Local-first

The system should work well in self-hosted and LAN-focused deployments before optimizing for internet-facing complexity.

### Progressive complexity

The design should start with strong core metadata, then expand into files, relationships, categories, ratings, and hierarchies in deliberate phases.

### Separation of concerns

Media-level metadata, file-level metadata, and playback-level descriptors should remain conceptually distinct even when linked.

## High-Level Functional Scope

The project should ultimately support these capability areas.

### Metadata management

- create and update media records
- enrich metadata over time
- support both minimal and rich metadata states

### Organization

- categories
- tags
- collections
- status/lifecycle management

### Relationship modeling

- related media items
- parent-child structures
- file attachments
- alternate versions or editions

### Retrieval

- list
- filter
- sort
- search
- detail fetch

### Playback enablement

- expose file/resource references
- support frontend playback selection
- support local-network-friendly access patterns

## Non-Goals

At least initially, the project should not try to be:

- a hosted multi-tenant SaaS
- a social media platform
- a DRM platform
- a universal transcoding farm
- a replacement for every media player UI

Those concerns may become relevant later, but they are not the core value of this backend.

## Success Criteria

The project is succeeding when:

- the backend models media as meaningful library objects, not just filenames
- a frontend can consume the service and render a polished browsing experience
- users can organize and curate local media metadata in a consistent way
- playback can be initiated from frontend-visible backend data
- the system remains practical for self-hosted LAN usage

## Relationship To Lower-Level Specs

This document defines the product-level why and what.

Lower-level specifications should refine it:

- [media-domain-spec.md](/home/isamudaison/Code/libre-media-manager/docs/media-domain-spec.md): concrete domain model, fields, enums, migration shape, and API evolution
- [taxonomy-spec.md](/home/isamudaison/Code/libre-media-manager/docs/taxonomy-spec.md): categories, tags, ratings, and classification design

Future lower-level specs may cover:

- media file modeling
- artwork and image modeling
- hierarchy design for series, seasons, episodes, albums, and tracks
- playback resource and streaming descriptor design

## Recommended Next Spec Layers

To turn this into implementation work, the next specification layers should be:

1. media domain model
2. taxonomy model for categories, tags, and ratings
3. media file and playback resource model
4. hierarchy model for composite media

## Recommendation

Use this document as the top-level project contract.

Use the current domain spec as the next layer down.

Do not implement hierarchical/composite concerns ad hoc.
They should follow from this project-level definition so the backend grows toward the intended frontend and playback use case rather than drifting into disconnected CRUD features.
