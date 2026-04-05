package net.creft.lmm.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Media {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String mediaId;

    @Column(nullable = false)
    private String title;

    @OneToMany(
            mappedBy = "media",
            cascade = jakarta.persistence.CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.EAGER
    )
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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
        mediaFile.setMedia(this);
        this.mediaFiles.add(mediaFile);
    }
}
