package net.creft.lmm.response;

import net.creft.lmm.model.Media;

public class MediaResponse {

    private Media media;

    public MediaResponse(Media media) {
        this.media = media;
    }

    public Media getMedia() {
        return media;
    }

    public void setMedia(Media media) {
        this.media = media;
    }
}
