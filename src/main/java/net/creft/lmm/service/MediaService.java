package net.creft.lmm.service;

import net.creft.lmm.model.Media;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MediaService {

    Page<Media> listMedia(String title, Pageable pageable);

    Media getMedia(String mediaId);

    Media createMedia(String title, List<MediaFileDraft> mediaFiles);

    Media updateMedia(String mediaId, String title, List<MediaFileDraft> mediaFiles);

    void deleteMedia(String mediaId);
}
