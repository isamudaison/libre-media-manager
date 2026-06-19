package net.creft.lmm.exception;

public class MediaFileVersionConflictException extends RuntimeException {

    public MediaFileVersionConflictException(String mediaFileId, Long expectedVersion, Long currentVersion) {
        super("Media file with id '" + mediaFileId + "' has version " + currentVersion
                + " but the request expected version " + expectedVersion);
    }
}
