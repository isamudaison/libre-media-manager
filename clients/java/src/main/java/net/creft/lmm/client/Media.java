package net.creft.lmm.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Media(
        String mediaId,
        String parentId,
        Long version,
        String title,
        String originalTitle,
        MediaType mediaType,
        MediaStatus status,
        String summary,
        String releaseDate,
        Integer runtimeMinutes,
        String language,
        String createdAt,
        String updatedAt,
        List<MediaFile> mediaFiles
) {
    public Media {
        mediaFiles = mediaFiles == null ? List.of() : List.copyOf(mediaFiles);
    }
}
