package net.creft.lmm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import net.creft.lmm.model.MediaStatus;
import net.creft.lmm.model.MediaType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CreateMediaRequest {
    @Schema(description = "Human-readable media title", example = "Arrival")
    @NotBlank(message = "title is required")
    @Size(max = 255, message = "title must be at most 255 characters")
    private String title;

    @Schema(description = "Original or source-language title", example = "Story of Your Life")
    @Size(max = 255, message = "originalTitle must be at most 255 characters")
    private String originalTitle;

    @Schema(description = "Catalog media type", example = "MOVIE")
    @NotNull(message = "mediaType is required")
    private MediaType mediaType;

    @Schema(description = "Lifecycle status; defaults to ACTIVE when omitted", example = "ACTIVE")
    private MediaStatus status;

    @Schema(description = "Human-readable summary", example = "A linguist is recruited to communicate with extraterrestrial visitors.")
    @Size(max = 4000, message = "summary must be at most 4000 characters")
    private String summary;

    @Schema(description = "Canonical release date", example = "2016-11-11")
    private LocalDate releaseDate;

    @Schema(description = "Runtime in minutes", example = "116")
    @Positive(message = "runtimeMinutes must be greater than 0")
    private Integer runtimeMinutes;

    @Schema(description = "Primary language code", example = "en")
    @Size(max = 16, message = "language must be at most 16 characters")
    private String language;

    @Valid
    @Schema(description = "Associated media files in playback order")
    private List<MediaFileRequest> mediaFiles = new ArrayList<>();

    public CreateMediaRequest() {
    }

    public CreateMediaRequest(String title, MediaType mediaType) {
        this.title = title;
        this.mediaType = mediaType;
    }

    public CreateMediaRequest(String title, MediaType mediaType, List<MediaFileRequest> mediaFiles) {
        this(title, null, mediaType, null, null, null, null, null, mediaFiles);
    }

    public CreateMediaRequest(
            String title,
            String originalTitle,
            MediaType mediaType,
            MediaStatus status,
            String summary,
            LocalDate releaseDate,
            Integer runtimeMinutes,
            String language,
            List<MediaFileRequest> mediaFiles
    ) {
        this.title = title;
        this.originalTitle = originalTitle;
        this.mediaType = mediaType;
        this.status = status;
        this.summary = summary;
        this.releaseDate = releaseDate;
        this.runtimeMinutes = runtimeMinutes;
        this.language = language;
        this.mediaFiles = mediaFiles == null ? new ArrayList<>() : new ArrayList<>(mediaFiles);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    public MediaStatus getStatus() {
        return status;
    }

    public void setStatus(MediaStatus status) {
        this.status = status;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    public Integer getRuntimeMinutes() {
        return runtimeMinutes;
    }

    public void setRuntimeMinutes(Integer runtimeMinutes) {
        this.runtimeMinutes = runtimeMinutes;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public List<MediaFileRequest> getMediaFiles() {
        return mediaFiles;
    }

    public void setMediaFiles(List<MediaFileRequest> mediaFiles) {
        this.mediaFiles = mediaFiles == null ? new ArrayList<>() : new ArrayList<>(mediaFiles);
    }
}
