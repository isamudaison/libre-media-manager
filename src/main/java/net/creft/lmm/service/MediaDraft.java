package net.creft.lmm.service;

import net.creft.lmm.model.MediaStatus;
import net.creft.lmm.model.MediaType;

import java.time.LocalDate;
import java.util.List;

public record MediaDraft(
        String title,
        String originalTitle,
        MediaType mediaType,
        MediaStatus status,
        String summary,
        LocalDate releaseDate,
        Integer runtimeMinutes,
        String language,
        String parentId,
        List<MediaFileDraft> mediaFiles
) {
    public MediaDraft {
        mediaFiles = mediaFiles == null ? List.of() : List.copyOf(mediaFiles);
    }
}
