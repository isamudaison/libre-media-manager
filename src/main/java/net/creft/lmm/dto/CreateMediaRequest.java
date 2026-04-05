package net.creft.lmm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

public class CreateMediaRequest {
    @Schema(description = "Human-readable media title", example = "Arrival")
    @NotBlank(message = "title is required")
    @Size(max = 255, message = "title must be at most 255 characters")
    private String title;

    @Valid
    @Schema(description = "Associated media files in playback order")
    private List<MediaFileRequest> mediaFiles = new ArrayList<>();

    public CreateMediaRequest() {
    }

    public CreateMediaRequest(String title) {
        this.title = title;
    }

    public CreateMediaRequest(String title, List<MediaFileRequest> mediaFiles) {
        this.title = title;
        this.mediaFiles = mediaFiles == null ? new ArrayList<>() : new ArrayList<>(mediaFiles);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<MediaFileRequest> getMediaFiles() {
        return mediaFiles;
    }

    public void setMediaFiles(List<MediaFileRequest> mediaFiles) {
        this.mediaFiles = mediaFiles == null ? new ArrayList<>() : new ArrayList<>(mediaFiles);
    }
}
