package net.creft.lmm.response;

import io.swagger.v3.oas.annotations.media.Schema;
import net.creft.lmm.model.MediaFile;

@Schema(description = "A playback-oriented file attached to a media item")
public class MediaFileResponse {
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

    public MediaFileResponse(
            String location,
            String label,
            String mimeType,
            Long sizeBytes,
            Integer durationSeconds,
            boolean primaryFile
    ) {
        this.location = location;
        this.label = label;
        this.mimeType = mimeType;
        this.sizeBytes = sizeBytes;
        this.durationSeconds = durationSeconds;
        this.primaryFile = primaryFile;
    }

    public static MediaFileResponse from(MediaFile mediaFile) {
        return new MediaFileResponse(
                mediaFile.getLocation(),
                mediaFile.getLabel(),
                mediaFile.getMimeType(),
                mediaFile.getSizeBytes(),
                mediaFile.getDurationSeconds(),
                mediaFile.isPrimaryFile()
        );
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
}
