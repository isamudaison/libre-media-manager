package net.creft.lmm.client;

import java.util.List;

public record UpdateMediaRequest(
        String title,
        Long version,
        String parentId,
        String originalTitle,
        MediaType mediaType,
        MediaStatus status,
        String summary,
        String releaseDate,
        Integer runtimeMinutes,
        String language,
        List<MediaFileRequest> mediaFiles
) {
    public UpdateMediaRequest {
        mediaFiles = mediaFiles == null ? List.of() : List.copyOf(mediaFiles);
    }
}
