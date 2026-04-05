package net.creft.lmm.service;

import net.creft.lmm.model.Media;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MediaService {

    Page<Media> listMedia(MediaSearchCriteria criteria, Pageable pageable);

    Media getMedia(String mediaId);

    Media createMedia(MediaDraft mediaDraft);

    Media updateMedia(String mediaId, MediaDraft mediaDraft);

    void deleteMedia(String mediaId);
}
