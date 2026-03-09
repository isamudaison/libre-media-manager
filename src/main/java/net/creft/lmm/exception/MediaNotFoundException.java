package net.creft.lmm.exception;

public class MediaNotFoundException extends RuntimeException {

    public MediaNotFoundException(String mediaId) {
        super("Media with id '" + mediaId + "' was not found");
    }
}
