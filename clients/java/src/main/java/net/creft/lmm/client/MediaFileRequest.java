package net.creft.lmm.client;

public record MediaFileRequest(
        String mediaFileId,
        Long version,
        String location,
        String label,
        String mimeType,
        Long sizeBytes,
        Integer durationSeconds,
        boolean primaryFile
) {
}
