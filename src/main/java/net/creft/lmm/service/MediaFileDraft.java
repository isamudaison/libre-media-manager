package net.creft.lmm.service;

public record MediaFileDraft(
        String mediaFileId,
        Long version,
        String location,
        String label,
        String mimeType,
        Long sizeBytes,
        Integer durationSeconds,
        boolean primaryFile
) {
    public MediaFileDraft(
            String location,
            String label,
            String mimeType,
            Long sizeBytes,
            Integer durationSeconds,
            boolean primaryFile
    ) {
        this(null, null, location, label, mimeType, sizeBytes, durationSeconds, primaryFile);
    }
}
