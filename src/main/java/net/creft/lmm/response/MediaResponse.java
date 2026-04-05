package net.creft.lmm.response;

import io.swagger.v3.oas.annotations.media.Schema;
import net.creft.lmm.model.Media;
import net.creft.lmm.model.MediaStatus;
import net.creft.lmm.model.MediaType;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "A single media record")
public class MediaResponse {
    @Schema(description = "Public media identifier", example = "c1c32f42-8919-4d6c-a0d8-9b4d42d2adbe")
    private final String mediaId;

    @Schema(description = "Optional parent media identifier for lightweight collection grouping", example = "b8a79f6d-9317-4d16-8c77-49b1a6f7ec28")
    private final String parentId;

    @Schema(description = "Human-readable media title", example = "Arrival")
    private final String title;

    @Schema(description = "Original or source-language title", example = "Story of Your Life")
    private final String originalTitle;

    @Schema(description = "Catalog media type", example = "MOVIE")
    private final MediaType mediaType;

    @Schema(description = "Lifecycle status", example = "ACTIVE")
    private final MediaStatus status;

    @Schema(description = "Human-readable summary", example = "A linguist is recruited to communicate with extraterrestrial visitors.")
    private final String summary;

    @Schema(description = "Canonical release date", example = "2016-11-11")
    private final LocalDate releaseDate;

    @Schema(description = "Runtime in minutes", example = "116")
    private final Integer runtimeMinutes;

    @Schema(description = "Primary language code", example = "en")
    private final String language;

    @Schema(description = "Creation timestamp", example = "2026-04-04T18:12:00Z")
    private final Instant createdAt;

    @Schema(description = "Last update timestamp", example = "2026-04-04T18:12:00Z")
    private final Instant updatedAt;

    @Schema(description = "Associated media files in playback order")
    private final List<MediaFileResponse> mediaFiles;

    public MediaResponse(
            String mediaId,
            String parentId,
            String title,
            String originalTitle,
            MediaType mediaType,
            MediaStatus status,
            String summary,
            LocalDate releaseDate,
            Integer runtimeMinutes,
            String language,
            Instant createdAt,
            Instant updatedAt,
            List<MediaFileResponse> mediaFiles
    ) {
        this.mediaId = mediaId;
        this.parentId = parentId;
        this.title = title;
        this.originalTitle = originalTitle;
        this.mediaType = mediaType;
        this.status = status;
        this.summary = summary;
        this.releaseDate = releaseDate;
        this.runtimeMinutes = runtimeMinutes;
        this.language = language;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.mediaFiles = List.copyOf(mediaFiles);
    }

    public static MediaResponse from(Media media) {
        List<MediaFileResponse> mediaFiles = media.getMediaFiles().stream()
                .map(MediaFileResponse::from)
                .toList();
        return new MediaResponse(
                media.getMediaId(),
                media.getParentId(),
                media.getTitle(),
                media.getOriginalTitle(),
                media.getMediaType(),
                media.getStatus(),
                media.getSummary(),
                media.getReleaseDate(),
                media.getRuntimeMinutes(),
                media.getLanguage(),
                media.getCreatedAt(),
                media.getUpdatedAt(),
                mediaFiles
        );
    }

    public String getMediaId() {
        return mediaId;
    }

    public String getParentId() {
        return parentId;
    }

    public String getTitle() {
        return title;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public MediaStatus getStatus() {
        return status;
    }

    public String getSummary() {
        return summary;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public Integer getRuntimeMinutes() {
        return runtimeMinutes;
    }

    public String getLanguage() {
        return language;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public List<MediaFileResponse> getMediaFiles() {
        return mediaFiles;
    }
}
