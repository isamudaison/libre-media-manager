package net.creft.lmm.dto;

public class CreateMediaRequest {
    private String title;

    public CreateMediaRequest() {
    }

    public CreateMediaRequest(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
