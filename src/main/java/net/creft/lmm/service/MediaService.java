package net.creft.lmm.service;

import net.creft.lmm.model.Media;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MediaService {

    Page<Media> listMedia(String title, Pageable pageable);

    Media getMedia(String mediaId);

    Media createMedia(String title);

    Media updateMedia(String mediaId, String title);

    void deleteMedia(String mediaId);
}
