package net.creft.lmm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class MediaFileRequest {
    @Schema(description = "Local or network-accessible file location", example = "/srv/media/arrival.mkv")
    @NotBlank(message = "mediaFiles[].location is required")
    @Size(max = 2048, message = "mediaFiles[].location must be at most 2048 characters")
    private String location;

    @Schema(description = "User-facing label for the file", example = "4K Main Feature")
    @Size(max = 255, message = "mediaFiles[].label must be at most 255 characters")
    private String label;

    @Schema(description = "Declared MIME type", example = "video/x-matroska")
    @Size(max = 255, message = "mediaFiles[].mimeType must be at most 255 characters")
    private String mimeType;

    @Schema(description = "File size in bytes", example = "7340032000")
    @Positive(message = "mediaFiles[].sizeBytes must be greater than 0")
    private Long sizeBytes;

    @Schema(description = "Runtime in seconds for this file", example = "6960")
    @Positive(message = "mediaFiles[].durationSeconds must be greater than 0")
    private Integer durationSeconds;

    @Schema(description = "Whether this file is the preferred playback target", example = "true")
    private boolean primaryFile;

    public MediaFileRequest() {
    }

    public MediaFileRequest(String location) {
        this.location = location;
    }

    public MediaFileRequest(
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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public Long getSizeBytes() {
        return sizeBytes;
    }

    public void setSizeBytes(Long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }

    public Integer getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Integer durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public boolean isPrimaryFile() {
        return primaryFile;
    }

    public void setPrimaryFile(boolean primaryFile) {
        this.primaryFile = primaryFile;
    }
}
