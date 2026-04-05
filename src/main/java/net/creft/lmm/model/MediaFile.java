package net.creft.lmm.model;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Column;

@Embeddable
public class MediaFile {

    @Column(nullable = false, length = 2048)
    private String location;

    @Column(length = 255)
    private String label;

    @Column(length = 255)
    private String mimeType;

    @Column
    private Long sizeBytes;

    @Column
    private Integer durationSeconds;

    @Column(nullable = false)
    private boolean primaryFile;

    public MediaFile() {
    }

    public MediaFile(String location) {
        this.location = location;
    }

    public MediaFile(
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
