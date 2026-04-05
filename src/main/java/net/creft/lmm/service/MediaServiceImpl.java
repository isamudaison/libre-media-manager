package net.creft.lmm.service;

import net.creft.lmm.exception.InvalidRequestParameterException;
import net.creft.lmm.exception.MediaNotFoundException;
import net.creft.lmm.model.Media;
import net.creft.lmm.model.MediaFile;
import net.creft.lmm.model.MediaStatus;
import net.creft.lmm.model.MediaType;
import net.creft.lmm.repository.MediaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class MediaServiceImpl implements MediaService {
    private static final Logger logger = LoggerFactory.getLogger(MediaServiceImpl.class);
    private static final String MULTIPLE_PRIMARY_FILES_MESSAGE =
            "mediaFiles can contain at most one primaryFile=true entry";

    private final MediaRepository mediaRepository;

    public MediaServiceImpl(MediaRepository mediaRepository) {
        this.mediaRepository = mediaRepository;
    }

    @Override
    public Page<Media> listMedia(MediaSearchCriteria criteria, Pageable pageable) {
        Specification<Media> specification = Specification.where(null);

        String normalizedTitle = normalizeNullableString(criteria.title());
        if (normalizedTitle != null) {
            specification = specification.and(titleContains(normalizedTitle));
        }

        if (criteria.mediaType() != null) {
            specification = specification.and(hasMediaType(criteria.mediaType()));
        }

        if (criteria.status() != null) {
            specification = specification.and(hasStatus(criteria.status()));
        }

        String normalizedLanguage = normalizeNullableString(criteria.language());
        if (normalizedLanguage != null) {
            specification = specification.and(hasLanguage(normalizedLanguage));
        }

        if (criteria.releasedBefore() != null) {
            specification = specification.and(releasedOnOrBefore(criteria.releasedBefore()));
        }

        if (criteria.releasedAfter() != null) {
            specification = specification.and(releasedOnOrAfter(criteria.releasedAfter()));
        }

        return mediaRepository.findAll(specification, pageable);
    }

    @Override
    public Media getMedia(String mediaId) {
        return fetchMediaOrThrow(mediaId);
    }

    @Override
    public Media createMedia(MediaDraft mediaDraft) {
        Media media = new Media();
        media.setMediaId(UUID.randomUUID().toString());
        applyMediaDraft(media, mediaDraft);
        return mediaRepository.save(media);
    }

    @Override
    public Media updateMedia(String mediaId, MediaDraft mediaDraft) {
        Media media = fetchMediaOrThrow(mediaId);
        applyMediaDraft(media, mediaDraft);
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

    private void applyMediaDraft(Media media, MediaDraft mediaDraft) {
        media.setTitle(normalizeRequiredString("title", mediaDraft.title()));
        media.setOriginalTitle(normalizeNullableString(mediaDraft.originalTitle()));
        media.setMediaType(requireMediaType(mediaDraft.mediaType()));
        media.setStatus(mediaDraft.status() == null ? MediaStatus.ACTIVE : mediaDraft.status());
        media.setSummary(normalizeNullableString(mediaDraft.summary()));
        media.setReleaseDate(mediaDraft.releaseDate());
        media.setRuntimeMinutes(validatePositiveInteger("runtimeMinutes", mediaDraft.runtimeMinutes()));
        media.setLanguage(normalizeNullableString(mediaDraft.language()));
        applyMediaFiles(media, mediaDraft.mediaFiles());
    }

    private void applyMediaFiles(Media media, List<MediaFileDraft> mediaFileDrafts) {
        List<MediaFile> mediaFiles = mediaFileDrafts == null ? List.of() : mediaFileDrafts.stream()
                .map(this::toMediaFile)
                .toList();
        validatePrimaryFileSelection(mediaFiles);
        media.replaceMediaFiles(mediaFiles);
    }

    private MediaFile toMediaFile(MediaFileDraft mediaFileDraft) {
        return new MediaFile(
                normalizeNullableString(mediaFileDraft.location()),
                normalizeNullableString(mediaFileDraft.label()),
                normalizeNullableString(mediaFileDraft.mimeType()),
                mediaFileDraft.sizeBytes(),
                mediaFileDraft.durationSeconds(),
                mediaFileDraft.primaryFile()
        );
    }

    private String normalizeNullableString(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeRequiredString(String field, String value) {
        if (value == null) {
            throw new InvalidRequestParameterException(field, field + " is required");
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new InvalidRequestParameterException(field, field + " is required");
        }
        return trimmed;
    }

    private MediaType requireMediaType(MediaType mediaType) {
        if (mediaType == null) {
            throw new InvalidRequestParameterException("mediaType", "mediaType is required");
        }
        return mediaType;
    }

    private Integer validatePositiveInteger(String field, Integer value) {
        if (value == null) {
            return null;
        }
        if (value <= 0) {
            throw new InvalidRequestParameterException(field, field + " must be greater than 0");
        }
        return value;
    }

    private void validatePrimaryFileSelection(List<MediaFile> mediaFiles) {
        long primaryFiles = mediaFiles.stream()
                .filter(MediaFile::isPrimaryFile)
                .count();
        if (primaryFiles > 1) {
            throw new InvalidRequestParameterException("mediaFiles", MULTIPLE_PRIMARY_FILES_MESSAGE);
        }
    }

    private Specification<Media> titleContains(String title) {
        return (root, query, builder) ->
                builder.like(builder.lower(root.get("title")), "%" + title.toLowerCase() + "%");
    }

    private Specification<Media> hasMediaType(MediaType mediaType) {
        return (root, query, builder) -> builder.equal(root.get("mediaType"), mediaType);
    }

    private Specification<Media> hasStatus(MediaStatus status) {
        return (root, query, builder) -> builder.equal(root.get("status"), status);
    }

    private Specification<Media> hasLanguage(String language) {
        return (root, query, builder) ->
                builder.equal(builder.lower(root.get("language")), language.toLowerCase());
    }

    private Specification<Media> releasedOnOrBefore(LocalDate releasedBefore) {
        return (root, query, builder) -> builder.lessThanOrEqualTo(root.get("releaseDate"), releasedBefore);
    }

    private Specification<Media> releasedOnOrAfter(LocalDate releasedAfter) {
        return (root, query, builder) -> builder.greaterThanOrEqualTo(root.get("releaseDate"), releasedAfter);
    }
}
