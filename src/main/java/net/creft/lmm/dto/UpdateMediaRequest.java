package net.creft.lmm.dto;

public class UpdateMediaRequest {
    private String title;

    public UpdateMediaRequest() {
    }

    public UpdateMediaRequest(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
