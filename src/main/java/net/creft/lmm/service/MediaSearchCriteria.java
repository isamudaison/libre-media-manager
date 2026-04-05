package net.creft.lmm.service;

import net.creft.lmm.model.MediaStatus;
import net.creft.lmm.model.MediaType;

import java.time.LocalDate;

public record MediaSearchCriteria(
        String title,
        MediaType mediaType,
        MediaStatus status,
        String language,
        LocalDate releasedBefore,
        LocalDate releasedAfter
) {
}
