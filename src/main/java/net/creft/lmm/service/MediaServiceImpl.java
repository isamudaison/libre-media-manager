package net.creft.lmm.service;

import net.creft.lmm.exception.MediaNotFoundException;
import net.creft.lmm.model.Media;
import net.creft.lmm.repository.MediaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class MediaServiceImpl implements MediaService {
    private static final Logger logger = LoggerFactory.getLogger(MediaServiceImpl.class);

    private final MediaRepository mediaRepository;

    public MediaServiceImpl(MediaRepository mediaRepository) {
        this.mediaRepository = mediaRepository;
    }

    @Override
    public Page<Media> listMedia(String title, Pageable pageable) {
        String normalizedTitle = title == null ? null : title.trim();
        if (normalizedTitle == null || normalizedTitle.isEmpty()) {
            return mediaRepository.findAll(pageable);
        }
        return mediaRepository.findByTitleContainingIgnoreCase(normalizedTitle, pageable);
    }

    @Override
    public Media getMedia(String mediaId) {
        return fetchMediaOrThrow(mediaId);
    }

    @Override
    public Media createMedia(String title) {
        Media media = new Media(UUID.randomUUID().toString(), title);
        return mediaRepository.save(media);
    }

    @Override
    public Media updateMedia(String mediaId, String title) {
        Media media = fetchMediaOrThrow(mediaId);
        media.setTitle(title);
        return mediaRepository.save(media);
    }

    @Override
    public void deleteMedia(String mediaId) {
        Media media = fetchMediaOrThrow(mediaId);
        mediaRepository.delete(media);
    }

    private Media fetchMediaOrThrow(String mediaId) {
        logger.debug("Fetching media with ID: {}", mediaId);
        Media media = mediaRepository.findByMediaId(mediaId);
        if (media == null) {
            logger.info("No media with ID: {}", mediaId);
            throw new MediaNotFoundException(mediaId);
        }
        logger.info("Fetched media with ID: {}", mediaId);
        return media;
    }
}
