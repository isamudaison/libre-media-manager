from __future__ import annotations

from dataclasses import dataclass, field, fields
from enum import Enum
from typing import Any


class MediaType(str, Enum):
    MOVIE = "MOVIE"
    EPISODE = "EPISODE"
    SERIES = "SERIES"
    COLLECTION = "COLLECTION"
    BOOK = "BOOK"
    AUDIOBOOK = "AUDIOBOOK"
    ALBUM = "ALBUM"
    PODCAST = "PODCAST"
    GAME = "GAME"
    OTHER = "OTHER"


class MediaStatus(str, Enum):
    DRAFT = "DRAFT"
    ACTIVE = "ACTIVE"
    ARCHIVED = "ARCHIVED"


def _enum_value(value: Enum | None) -> str | None:
    return None if value is None else value.value


def _compact_dict(payload: dict[str, Any]) -> dict[str, Any]:
    compacted: dict[str, Any] = {}
    for key, value in payload.items():
        if value is None:
            continue
        if isinstance(value, list):
            compacted[key] = [item.to_dict() if hasattr(item, "to_dict") else item for item in value]
            continue
        if isinstance(value, Enum):
            compacted[key] = value.value
            continue
        compacted[key] = value
    return compacted


def _filter_fields(model_type: type, payload: dict[str, Any]) -> dict[str, Any]:
    allowed = {item.name for item in fields(model_type)}
    return {key: value for key, value in payload.items() if key in allowed}


@dataclass(slots=True)
class MediaFileRequest:
    location: str
    mediaFileId: str | None = None
    version: int | None = None
    label: str | None = None
    mimeType: str | None = None
    sizeBytes: int | None = None
    durationSeconds: int | None = None
    primaryFile: bool = False

    def to_dict(self) -> dict[str, Any]:
        return _compact_dict(
            {
                "mediaFileId": self.mediaFileId,
                "version": self.version,
                "location": self.location,
                "label": self.label,
                "mimeType": self.mimeType,
                "sizeBytes": self.sizeBytes,
                "durationSeconds": self.durationSeconds,
                "primaryFile": self.primaryFile,
            }
        )


@dataclass(slots=True)
class CreateMediaRequest:
    title: str
    mediaType: MediaType
    parentId: str | None = None
    originalTitle: str | None = None
    status: MediaStatus | None = None
    summary: str | None = None
    releaseDate: str | None = None
    runtimeMinutes: int | None = None
    language: str | None = None
    mediaFiles: list[MediaFileRequest] = field(default_factory=list)

    def to_dict(self) -> dict[str, Any]:
        return _compact_dict(
            {
                "title": self.title,
                "parentId": self.parentId,
                "originalTitle": self.originalTitle,
                "mediaType": _enum_value(self.mediaType),
                "status": _enum_value(self.status),
                "summary": self.summary,
                "releaseDate": self.releaseDate,
                "runtimeMinutes": self.runtimeMinutes,
                "language": self.language,
                "mediaFiles": self.mediaFiles,
            }
        )


@dataclass(slots=True)
class UpdateMediaRequest:
    title: str
    version: int
    mediaType: MediaType
    parentId: str | None = None
    originalTitle: str | None = None
    status: MediaStatus | None = None
    summary: str | None = None
    releaseDate: str | None = None
    runtimeMinutes: int | None = None
    language: str | None = None
    mediaFiles: list[MediaFileRequest] = field(default_factory=list)

    def to_dict(self) -> dict[str, Any]:
        return _compact_dict(
            {
                "title": self.title,
                "version": self.version,
                "parentId": self.parentId,
                "originalTitle": self.originalTitle,
                "mediaType": _enum_value(self.mediaType),
                "status": _enum_value(self.status),
                "summary": self.summary,
                "releaseDate": self.releaseDate,
                "runtimeMinutes": self.runtimeMinutes,
                "language": self.language,
                "mediaFiles": self.mediaFiles,
            }
        )


@dataclass(slots=True)
class MediaFile:
    mediaFileId: str
    location: str
    label: str | None = None
    mimeType: str | None = None
    sizeBytes: int | None = None
    durationSeconds: int | None = None
    primaryFile: bool = False
    version: int | None = None
    createdAt: str | None = None
    updatedAt: str | None = None

    @classmethod
    def from_dict(cls, payload: dict[str, Any]) -> "MediaFile":
        return cls(**_filter_fields(cls, payload))


@dataclass(slots=True)
class Media:
    mediaId: str
    parentId: str | None = None
    version: int | None = None
    title: str = ""
    originalTitle: str | None = None
    mediaType: str | None = None
    status: str | None = None
    summary: str | None = None
    releaseDate: str | None = None
    runtimeMinutes: int | None = None
    language: str | None = None
    createdAt: str | None = None
    updatedAt: str | None = None
    mediaFiles: list[MediaFile] = field(default_factory=list)

    @classmethod
    def from_dict(cls, payload: dict[str, Any]) -> "Media":
        filtered = _filter_fields(cls, payload)
        media_files = payload.get("mediaFiles")
        filtered["mediaFiles"] = [
            MediaFile.from_dict(item) for item in media_files if isinstance(item, dict)
        ] if isinstance(media_files, list) else []
        return cls(**filtered)


@dataclass(slots=True)
class MediaPage:
    items: list[Media] = field(default_factory=list)
    page: int = 0
    size: int = 0
    totalElements: int = 0
    totalPages: int = 0

    @classmethod
    def from_dict(cls, payload: dict[str, Any]) -> "MediaPage":
        items = payload.get("items")
        return cls(
            items=[Media.from_dict(item) for item in items if isinstance(item, dict)] if isinstance(items, list) else [],
            page=int(payload.get("page", 0)),
            size=int(payload.get("size", 0)),
            totalElements=int(payload.get("totalElements", 0)),
            totalPages=int(payload.get("totalPages", 0)),
        )


@dataclass(slots=True)
class MediaListQuery:
    title: str | None = None
    parentId: str | None = None
    mediaType: MediaType | None = None
    status: MediaStatus | None = None
    language: str | None = None
    releasedBefore: str | None = None
    releasedAfter: str | None = None
    page: int | None = None
    size: int | None = None
    sort: str | None = None
    direction: str | None = None

    def to_query_params(self) -> dict[str, str]:
        params = _compact_dict(
            {
                "title": self.title,
                "parentId": self.parentId,
                "mediaType": _enum_value(self.mediaType),
                "status": _enum_value(self.status),
                "language": self.language,
                "releasedBefore": self.releasedBefore,
                "releasedAfter": self.releasedAfter,
                "page": str(self.page) if self.page is not None else None,
                "size": str(self.size) if self.size is not None else None,
                "sort": self.sort,
                "direction": self.direction,
            }
        )
        return {key: str(value) for key, value in params.items()}
