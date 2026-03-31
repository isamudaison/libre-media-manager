package net.creft.lmm.response;

import io.swagger.v3.oas.annotations.media.Schema;
import net.creft.lmm.model.Media;

@Schema(description = "A single media record")
public class MediaResponse {
    @Schema(description = "Public media identifier", example = "c1c32f42-8919-4d6c-a0d8-9b4d42d2adbe")
    private final String mediaId;

    @Schema(description = "Human-readable media title", example = "Arrival")
    private final String title;

    public MediaResponse(String mediaId, String title) {
        this.mediaId = mediaId;
        this.title = title;
    }

    public static MediaResponse from(Media media) {
        return new MediaResponse(media.getMediaId(), media.getTitle());
    }

    public String getMediaId() {
        return mediaId;
    }

    public String getTitle() {
        return title;
    }
}
