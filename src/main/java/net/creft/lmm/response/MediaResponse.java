package net.creft.lmm.response;

import io.swagger.v3.oas.annotations.media.Schema;
import net.creft.lmm.model.Media;

import java.util.List;

@Schema(description = "A single media record")
public class MediaResponse {
    @Schema(description = "Public media identifier", example = "c1c32f42-8919-4d6c-a0d8-9b4d42d2adbe")
    private final String mediaId;

    @Schema(description = "Human-readable media title", example = "Arrival")
    private final String title;

    @Schema(description = "Associated media files in playback order")
    private final List<MediaFileResponse> mediaFiles;

    public MediaResponse(String mediaId, String title, List<MediaFileResponse> mediaFiles) {
        this.mediaId = mediaId;
        this.title = title;
        this.mediaFiles = List.copyOf(mediaFiles);
    }

    public static MediaResponse from(Media media) {
        List<MediaFileResponse> mediaFiles = media.getMediaFiles().stream()
                .map(MediaFileResponse::from)
                .toList();
        return new MediaResponse(media.getMediaId(), media.getTitle(), mediaFiles);
    }

    public String getMediaId() {
        return mediaId;
    }

    public String getTitle() {
        return title;
    }

    public List<MediaFileResponse> getMediaFiles() {
        return mediaFiles;
    }
}
