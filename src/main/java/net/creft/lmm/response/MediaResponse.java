package net.creft.lmm.response;

public class MediaResponse {

    private String mediaId;
    private String title;

    public MediaResponse(String mediaId, String title) {
        this.mediaId = mediaId;
        this.title = title;
    }

    public String getMediaId() {
        return mediaId;
    }

    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
