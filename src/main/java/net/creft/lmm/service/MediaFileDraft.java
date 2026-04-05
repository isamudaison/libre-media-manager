package net.creft.lmm.service;

public record MediaFileDraft(
        String location,
        String label,
        String mimeType,
        Long sizeBytes,
        Integer durationSeconds,
        boolean primaryFile
) {
}
