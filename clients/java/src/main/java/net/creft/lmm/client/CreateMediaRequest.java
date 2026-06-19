package net.creft.lmm.client;

import java.util.List;

public record CreateMediaRequest(
        String title,
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
    public CreateMediaRequest {
        mediaFiles = mediaFiles == null ? List.of() : List.copyOf(mediaFiles);
    }
}
