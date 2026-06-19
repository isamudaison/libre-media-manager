package net.creft.lmm.response;

import io.swagger.v3.oas.annotations.media.Schema;
import net.creft.lmm.model.MediaFile;

import java.time.Instant;

@Schema(description = "A playback-oriented file attached to a media item")
public class MediaFileResponse {
    @Schema(description = "Public media file identifier", example = "6f27d761-3c7f-4a1c-b0ea-9a0d7b77cb17")
    private final String mediaFileId;

    @Schema(description = "Local or network-accessible file location", example = "/srv/media/arrival.mkv")
    private final String location;

    @Schema(description = "User-facing label for the file", example = "4K Main Feature")
    private final String label;

    @Schema(description = "Declared MIME type", example = "video/x-matroska")
    private final String mimeType;

    @Schema(description = "File size in bytes", example = "7340032000")
    private final Long sizeBytes;

    @Schema(description = "Runtime in seconds for this file", example = "6960")
    private final Integer durationSeconds;

    @Schema(description = "Whether this file is the preferred playback target", example = "true")
    private final boolean primaryFile;

    @Schema(description = "Server-managed optimistic-lock version", example = "0")
    private final Long version;

    @Schema(description = "Creation timestamp", example = "2026-06-19T18:12:00Z")
    private final Instant createdAt;

    @Schema(description = "Last update timestamp", example = "2026-06-19T18:12:00Z")
    private final Instant updatedAt;

    public MediaFileResponse(
            String mediaFileId,
            String location,
            String label,
            String mimeType,
            Long sizeBytes,
            Integer durationSeconds,
            boolean primaryFile,
            Long version,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.mediaFileId = mediaFileId;
        this.location = location;
        this.label = label;
        this.mimeType = mimeType;
        this.sizeBytes = sizeBytes;
        this.durationSeconds = durationSeconds;
        this.primaryFile = primaryFile;
        this.version = version;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static MediaFileResponse from(MediaFile mediaFile) {
        return new MediaFileResponse(
                mediaFile.getMediaFileId(),
                mediaFile.getLocation(),
                mediaFile.getLabel(),
                mediaFile.getMimeType(),
                mediaFile.getSizeBytes(),
                mediaFile.getDurationSeconds(),
                mediaFile.isPrimaryFile(),
                mediaFile.getVersion(),
                mediaFile.getCreatedAt(),
                mediaFile.getUpdatedAt()
        );
    }

    public String getMediaFileId() {
        return mediaFileId;
    }

    public String getLocation() {
        return location;
    }

    public String getLabel() {
        return label;
    }

    public String getMimeType() {
        return mimeType;
    }

    public Long getSizeBytes() {
        return sizeBytes;
    }

    public Integer getDurationSeconds() {
        return durationSeconds;
    }

    public boolean isPrimaryFile() {
        return primaryFile;
    }

    public Long getVersion() {
        return version;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
