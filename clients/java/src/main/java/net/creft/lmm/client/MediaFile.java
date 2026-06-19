package net.creft.lmm.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MediaFile(
        String mediaFileId,
        String location,
        String label,
        String mimeType,
        Long sizeBytes,
        Integer durationSeconds,
        boolean primaryFile,
        Long version,
        String createdAt,
        String updatedAt
) {
}
