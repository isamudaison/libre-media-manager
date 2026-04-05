package net.creft.lmm.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Media {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 36)
    private String mediaId;

    @Column(length = 36)
    private String parentId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 255)
    private String originalTitle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private MediaType mediaType = MediaType.OTHER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private MediaStatus status = MediaStatus.ACTIVE;

    @Column(length = 4000)
    private String summary;

    @Column
    private LocalDate releaseDate;

    @Column
    private Integer runtimeMinutes;

    @Column(length = 16)
    private String language;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "media_file", joinColumns = @JoinColumn(name = "media_id", referencedColumnName = "id"))
    @OrderColumn(name = "file_order")
    private List<MediaFile> mediaFiles = new ArrayList<>();

    public Media() {
    }

    public Media(String mediaId, String title) {
        this.mediaId = mediaId;
        this.title = title;
    }

    public Media(String mediaId, String title, List<MediaFile> mediaFiles) {
        this(mediaId, title);
        replaceMediaFiles(mediaFiles);
    }

    // Getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMediaId() {
        return mediaId;
    }

    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<MediaFile> getMediaFiles() {
        return mediaFiles;
    }

    public void setMediaFiles(List<MediaFile> mediaFiles) {
        replaceMediaFiles(mediaFiles);
    }

    public void replaceMediaFiles(List<MediaFile> mediaFiles) {
        this.mediaFiles.clear();
        if (mediaFiles == null) {
            return;
        }
        for (MediaFile mediaFile : mediaFiles) {
            addMediaFile(mediaFile);
        }
    }

    public void addMediaFile(MediaFile mediaFile) {
        this.mediaFiles.add(mediaFile);
    }

    @PrePersist
    public void onCreate() {
        Instant now = Instant.now();
        if (mediaType == null) {
            mediaType = MediaType.OTHER;
        }
        if (status == null) {
            status = MediaStatus.ACTIVE;
        }
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    public void onUpdate() {
        if (mediaType == null) {
            mediaType = MediaType.OTHER;
        }
        if (status == null) {
            status = MediaStatus.ACTIVE;
        }
        updatedAt = Instant.now();
    }
}
