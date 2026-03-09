package net.creft.lmm.service;

import net.creft.lmm.model.Media;

public interface MediaService {

    Media getMedia(String mediaId);

    Media createMedia(String title);

    Media updateMedia(String mediaId, String title);

    void deleteMedia(String mediaId);
}
