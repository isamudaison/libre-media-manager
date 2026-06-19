package net.creft.lmm.exception;

public class MediaVersionConflictException extends RuntimeException {

    public MediaVersionConflictException(String mediaId, Long expectedVersion, Long currentVersion) {
        super("Media with id '" + mediaId + "' has version " + currentVersion
                + " but the request expected version " + expectedVersion);
    }
}
