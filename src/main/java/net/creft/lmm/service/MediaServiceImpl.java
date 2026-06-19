package net.creft.lmm.service;

import net.creft.lmm.exception.InvalidRequestParameterException;
import net.creft.lmm.exception.MediaFileVersionConflictException;
import net.creft.lmm.exception.MediaNotFoundException;
import net.creft.lmm.exception.MediaVersionConflictException;
import net.creft.lmm.model.Media;
import net.creft.lmm.model.MediaFile;
import net.creft.lmm.model.MediaStatus;
import net.creft.lmm.model.MediaType;
import net.creft.lmm.repository.MediaFileRepository;
import net.creft.lmm.repository.MediaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class MediaServiceImpl implements MediaService {
    private static final Logger logger = LoggerFactory.getLogger(MediaServiceImpl.class);
    private static final String MULTIPLE_PRIMARY_FILES_MESSAGE =
            "mediaFiles can contain at most one primaryFile=true entry";
    private static final String INVALID_PARENT_ID_MESSAGE = "parentId must reference an existing mediaId";
    private static final String SELF_PARENT_ID_MESSAGE = "parentId cannot reference the same media item";
    private static final String CYCLIC_PARENT_ID_MESSAGE = "parentId cannot create a cycle";
    private static final String UNKNOWN_MEDIA_FILE_MESSAGE = "mediaFiles references an unknown mediaFileId";
    private static final String DUPLICATE_MEDIA_FILE_REFERENCE_MESSAGE =
            "mediaFiles cannot reference the same mediaFileId more than once";

    private final MediaRepository mediaRepository;
    private final MediaFileRepository mediaFileRepository;

    public MediaServiceImpl(MediaRepository mediaRepository, MediaFileRepository mediaFileRepository) {
        this.mediaRepository = mediaRepository;
        this.mediaFileRepository = mediaFileRepository;
    }

    @Override
    public Page<Media> listMedia(MediaSearchCriteria criteria, Pageable pageable) {
        Specification<Media> specification = Specification.where(null);

        String normalizedTitle = normalizeNullableString(criteria.title());
        if (normalizedTitle != null) {
            specification = specification.and(titleContains(normalizedTitle));
        }

        String normalizedParentId = normalizeNullableString(criteria.parentId());
        if (normalizedParentId != null) {
            specification = specification.and(hasParentId(normalizedParentId));
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

        Page<Media> mediaPage = mediaRepository.findAll(specification, pageable);
        hydrateMediaFiles(mediaPage.getContent());
        return mediaPage;
    }

    @Override
    public Media getMedia(String mediaId) {
        return hydrateMediaFiles(fetchMediaOrThrow(mediaId));
    }

    @Override
    @Transactional
    public Media createMedia(MediaDraft mediaDraft) {
        Media media = new Media();
        media.setMediaId(UUID.randomUUID().toString());
        validateMediaFiles(mediaDraft.mediaFiles());
        applyMediaDraft(media, mediaDraft);
        Media savedMedia = mediaRepository.save(media);
        savedMedia.setMediaFiles(syncMediaFiles(savedMedia.getMediaId(), mediaDraft.mediaFiles()));
        return savedMedia;
    }

    @Override
    @Transactional
    public Media updateMedia(String mediaId, Long expectedVersion, MediaDraft mediaDraft) {
        Media media = fetchMediaOrThrow(mediaId);
        validateVersion(mediaId, expectedVersion, media.getVersion());
        validateMediaFiles(mediaDraft.mediaFiles());
        applyMediaDraft(media, mediaDraft);
        media.setUpdatedAt(Instant.now());
        Media savedMedia = mediaRepository.save(media);
        savedMedia.setMediaFiles(syncMediaFiles(savedMedia.getMediaId(), mediaDraft.mediaFiles()));
        return savedMedia;
    }

    @Override
    @Transactional
    public void deleteMedia(String mediaId) {
        Media media = fetchMediaOrThrow(mediaId);
        detachMediaFiles(mediaId);
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

    private Media hydrateMediaFiles(Media media) {
        media.setMediaFiles(mediaFileRepository.findAllByMediaIdOrderByFileOrderAscIdAsc(media.getMediaId()));
        return media;
    }

    private void hydrateMediaFiles(List<Media> mediaItems) {
        if (mediaItems.isEmpty()) {
            return;
        }

        List<String> mediaIds = mediaItems.stream()
                .map(Media::getMediaId)
                .toList();

        Map<String, List<MediaFile>> filesByMediaId = new LinkedHashMap<>();
        for (MediaFile mediaFile : mediaFileRepository.findAllByMediaIdInOrderByMediaIdAscFileOrderAscIdAsc(mediaIds)) {
            filesByMediaId.computeIfAbsent(mediaFile.getMediaId(), ignored -> new ArrayList<>()).add(mediaFile);
        }

        for (Media media : mediaItems) {
            media.setMediaFiles(filesByMediaId.getOrDefault(media.getMediaId(), List.of()));
        }
    }

    private void applyMediaDraft(Media media, MediaDraft mediaDraft) {
        media.setTitle(normalizeRequiredString("title", mediaDraft.title()));
        String normalizedParentId = normalizeNullableString(mediaDraft.parentId());
        validateParentRelationship(media.getMediaId(), normalizedParentId);
        media.setParentId(normalizedParentId);
        media.setOriginalTitle(normalizeNullableString(mediaDraft.originalTitle()));
        media.setMediaType(requireMediaType(mediaDraft.mediaType()));
        media.setStatus(mediaDraft.status() == null ? MediaStatus.ACTIVE : mediaDraft.status());
        media.setSummary(normalizeNullableString(mediaDraft.summary()));
        media.setReleaseDate(mediaDraft.releaseDate());
        media.setRuntimeMinutes(validatePositiveInteger("runtimeMinutes", mediaDraft.runtimeMinutes()));
        media.setLanguage(normalizeNullableString(mediaDraft.language()));
    }

    private List<MediaFile> syncMediaFiles(String mediaId, List<MediaFileDraft> mediaFileDrafts) {
        List<MediaFileDraft> drafts = mediaFileDrafts == null ? List.of() : mediaFileDrafts;
        List<MediaFile> currentlyAssociatedFiles = mediaFileRepository.findAllByMediaIdOrderByFileOrderAscIdAsc(mediaId);
        Set<String> requestedMediaFileIds = validateAndCollectRequestedMediaFileIds(drafts);
        Set<String> mediaIdsToTouch = new HashSet<>();

        List<MediaFile> filesToPersist = new ArrayList<>();
        for (MediaFile existingFile : currentlyAssociatedFiles) {
            if (!requestedMediaFileIds.contains(existingFile.getMediaFileId())) {
                existingFile.setMediaId(null);
                existingFile.setFileOrder(null);
                existingFile.setPrimaryFile(false);
                filesToPersist.add(existingFile);
            }
        }

        List<MediaFile> associatedFiles = new ArrayList<>();
        for (int index = 0; index < drafts.size(); index++) {
            MediaFileDraft draft = drafts.get(index);
            MediaFile mediaFile = resolveMediaFileDraft(draft);
            String previousMediaId = normalizeNullableString(mediaFile.getMediaId());
            if (previousMediaId != null && !previousMediaId.equals(mediaId)) {
                mediaIdsToTouch.add(previousMediaId);
            }
            applyMediaFileDraft(mediaFile, mediaId, index, draft);
            associatedFiles.add(mediaFile);
            filesToPersist.add(mediaFile);
        }

        if (!filesToPersist.isEmpty()) {
            mediaFileRepository.saveAll(filesToPersist);
        }
        touchMediaItems(mediaIdsToTouch);

        return associatedFiles;
    }

    private void detachMediaFiles(String mediaId) {
        List<MediaFile> associatedFiles = mediaFileRepository.findAllByMediaIdOrderByFileOrderAscIdAsc(mediaId);
        if (associatedFiles.isEmpty()) {
            return;
        }

        for (MediaFile mediaFile : associatedFiles) {
            mediaFile.setMediaId(null);
            mediaFile.setFileOrder(null);
            mediaFile.setPrimaryFile(false);
        }
        mediaFileRepository.saveAll(associatedFiles);
    }

    private void validateMediaFiles(List<MediaFileDraft> mediaFileDrafts) {
        List<MediaFileDraft> drafts = mediaFileDrafts == null ? List.of() : mediaFileDrafts;
        validatePrimaryFileSelection(drafts);
        validateAndCollectRequestedMediaFileIds(drafts);
    }

    private void touchMediaItems(Set<String> mediaIds) {
        Instant now = Instant.now();
        for (String candidateMediaId : mediaIds) {
            Media media = mediaRepository.findByMediaId(candidateMediaId);
            if (media == null) {
                continue;
            }
            media.setUpdatedAt(now);
            mediaRepository.save(media);
        }
    }

    private Set<String> validateAndCollectRequestedMediaFileIds(List<MediaFileDraft> drafts) {
        Set<String> mediaFileIds = new HashSet<>();
        for (MediaFileDraft draft : drafts) {
            String normalizedMediaFileId = normalizeNullableString(draft.mediaFileId());
            if (normalizedMediaFileId != null && !mediaFileIds.add(normalizedMediaFileId)) {
                throw new InvalidRequestParameterException("mediaFiles", DUPLICATE_MEDIA_FILE_REFERENCE_MESSAGE);
            }
        }
        return mediaFileIds;
    }

    private MediaFile resolveMediaFileDraft(MediaFileDraft draft) {
        String normalizedMediaFileId = normalizeNullableString(draft.mediaFileId());
        if (normalizedMediaFileId == null) {
            return new MediaFile();
        }

        MediaFile mediaFile = mediaFileRepository.findByMediaFileId(normalizedMediaFileId);
        if (mediaFile == null) {
            throw new InvalidRequestParameterException("mediaFiles", UNKNOWN_MEDIA_FILE_MESSAGE);
        }
        validateMediaFileVersion(normalizedMediaFileId, draft.version(), mediaFile.getVersion());
        return mediaFile;
    }

    private void applyMediaFileDraft(MediaFile mediaFile, String mediaId, int fileOrder, MediaFileDraft mediaFileDraft) {
        mediaFile.setMediaId(mediaId);
        mediaFile.setFileOrder(fileOrder);
        mediaFile.setLocation(normalizeRequiredString("mediaFiles[].location", mediaFileDraft.location()));
        mediaFile.setLabel(normalizeNullableString(mediaFileDraft.label()));
        mediaFile.setMimeType(normalizeNullableString(mediaFileDraft.mimeType()));
        mediaFile.setSizeBytes(mediaFileDraft.sizeBytes());
        mediaFile.setDurationSeconds(mediaFileDraft.durationSeconds());
        mediaFile.setPrimaryFile(mediaFileDraft.primaryFile());
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

    private void validatePrimaryFileSelection(List<MediaFileDraft> mediaFiles) {
        long primaryFiles = mediaFiles.stream()
                .filter(MediaFileDraft::primaryFile)
                .count();
        if (primaryFiles > 1) {
            throw new InvalidRequestParameterException("mediaFiles", MULTIPLE_PRIMARY_FILES_MESSAGE);
        }
    }

    private void validateParentRelationship(String mediaId, String parentId) {
        if (parentId == null) {
            return;
        }
        if (parentId.equals(mediaId)) {
            throw new InvalidRequestParameterException("parentId", SELF_PARENT_ID_MESSAGE);
        }

        Media parent = mediaRepository.findByMediaId(parentId);
        if (parent == null) {
            throw new InvalidRequestParameterException("parentId", INVALID_PARENT_ID_MESSAGE);
        }

        validateNoParentCycle(mediaId, parent);
    }

    private void validateNoParentCycle(String mediaId, Media parent) {
        Set<String> visitedMediaIds = new HashSet<>();
        Media current = parent;
        while (current != null) {
            String currentMediaId = current.getMediaId();
            if (!visitedMediaIds.add(currentMediaId) || currentMediaId.equals(mediaId)) {
                throw new InvalidRequestParameterException("parentId", CYCLIC_PARENT_ID_MESSAGE);
            }

            String nextParentId = normalizeNullableString(current.getParentId());
            if (nextParentId == null) {
                return;
            }

            current = mediaRepository.findByMediaId(nextParentId);
        }
    }

    private void validateVersion(String mediaId, Long expectedVersion, Long currentVersion) {
        if (!Objects.equals(expectedVersion, currentVersion)) {
            throw new MediaVersionConflictException(mediaId, expectedVersion, currentVersion);
        }
    }

    private void validateMediaFileVersion(String mediaFileId, Long expectedVersion, Long currentVersion) {
        if (!Objects.equals(expectedVersion, currentVersion)) {
            throw new MediaFileVersionConflictException(mediaFileId, expectedVersion, currentVersion);
        }
    }

    private Specification<Media> titleContains(String title) {
        return (root, query, builder) ->
                builder.like(builder.lower(root.get("title")), "%" + title.toLowerCase(Locale.ROOT) + "%");
    }

    private Specification<Media> hasParentId(String parentId) {
        return (root, query, builder) -> builder.equal(root.get("parentId"), parentId);
    }

    private Specification<Media> hasMediaType(MediaType mediaType) {
        return (root, query, builder) -> builder.equal(root.get("mediaType"), mediaType);
    }

    private Specification<Media> hasStatus(MediaStatus status) {
        return (root, query, builder) -> builder.equal(root.get("status"), status);
    }

    private Specification<Media> hasLanguage(String language) {
        return (root, query, builder) ->
                builder.equal(builder.lower(root.get("language")), language.toLowerCase(Locale.ROOT));
    }

    private Specification<Media> releasedOnOrBefore(LocalDate releasedBefore) {
        return (root, query, builder) -> builder.lessThanOrEqualTo(root.get("releaseDate"), releasedBefore);
    }

    private Specification<Media> releasedOnOrAfter(LocalDate releasedAfter) {
        return (root, query, builder) -> builder.greaterThanOrEqualTo(root.get("releaseDate"), releasedAfter);
    }
}
