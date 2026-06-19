package net.creft.lmm.repository;

import net.creft.lmm.model.Media;
import net.creft.lmm.model.MediaFile;
import net.creft.lmm.model.MediaStatus;
import net.creft.lmm.model.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest(showSql = false)
@ActiveProfiles("test")
class MediaRepositoryTest {

    @Autowired
    private MediaRepository mediaRepository;

    @Autowired
    private MediaFileRepository mediaFileRepository;

    @Test
    void saveDuplicateMediaIdShouldFail() {
        mediaRepository.saveAndFlush(new Media("media-1", "First title"));

        Media duplicateMedia = new Media("media-1", "Second title");
        assertThrows(DataIntegrityViolationException.class, () -> mediaRepository.saveAndFlush(duplicateMedia));
    }

    @Test
    void saveNullTitleShouldFail() {
        Media media = new Media("media-2", null);
        assertThrows(DataIntegrityViolationException.class, () -> mediaRepository.saveAndFlush(media));
    }

    @Test
    void saveNullMediaIdShouldFail() {
        Media media = new Media(null, "Title");
        assertThrows(DataIntegrityViolationException.class, () -> mediaRepository.saveAndFlush(media));
    }

    @Test
    void saveStandaloneMediaFiles_PersistsOrderedAssociatedRows() {
        Media savedMedia = mediaRepository.saveAndFlush(new Media("media-3", "Arrival"));

        MediaFile firstFile = new MediaFile(
                "/srv/media/arrival-1080p.mkv",
                "1080p Encode",
                "video/x-matroska",
                4831838208L,
                6960,
                false
        );
        firstFile.setMediaId(savedMedia.getMediaId());
        firstFile.setFileOrder(0);

        MediaFile secondFile = new MediaFile(
                "/srv/media/arrival-4k.mkv",
                "4K Remux",
                "video/x-matroska",
                16831838208L,
                6960,
                true
        );
        secondFile.setMediaId(savedMedia.getMediaId());
        secondFile.setFileOrder(1);

        mediaFileRepository.saveAll(List.of(firstFile, secondFile));
        mediaFileRepository.flush();

        List<MediaFile> savedFiles = mediaFileRepository.findAllByMediaIdOrderByFileOrderAscIdAsc(savedMedia.getMediaId());

        assertEquals(MediaType.OTHER, savedMedia.getMediaType());
        assertEquals(MediaStatus.ACTIVE, savedMedia.getStatus());
        assertEquals(0L, savedMedia.getVersion());
        assertEquals(2, savedFiles.size());
        assertEquals("/srv/media/arrival-1080p.mkv", savedFiles.get(0).getLocation());
        assertEquals("/srv/media/arrival-4k.mkv", savedFiles.get(1).getLocation());
        assertEquals("1080p Encode", savedFiles.get(0).getLabel());
        assertEquals("video/x-matroska", savedFiles.get(0).getMimeType());
        assertEquals(4831838208L, savedFiles.get(0).getSizeBytes());
        assertEquals(6960, savedFiles.get(0).getDurationSeconds());
        assertEquals(false, savedFiles.get(0).isPrimaryFile());
        assertNotNull(savedFiles.get(0).getMediaFileId());
        assertNotNull(savedFiles.get(0).getCreatedAt());
        assertNotNull(savedFiles.get(0).getUpdatedAt());
        assertEquals(0L, savedFiles.get(0).getVersion());
        assertEquals("4K Remux", savedFiles.get(1).getLabel());
        assertEquals(true, savedFiles.get(1).isPrimaryFile());
    }

    @Test
    void saveMediaWithRichMetadata_PersistsScalarFieldsAndTimestamps() {
        mediaRepository.saveAndFlush(new Media("collection-1", "Collection"));

        Media media = new Media("media-4", "Arrival");
        media.setParentId("collection-1");
        media.setOriginalTitle("Story of Your Life");
        media.setMediaType(MediaType.MOVIE);
        media.setStatus(MediaStatus.ARCHIVED);
        media.setSummary("A linguist is recruited to communicate with extraterrestrial visitors.");
        media.setReleaseDate(LocalDate.parse("2016-11-11"));
        media.setRuntimeMinutes(116);
        media.setLanguage("en");

        Media saved = mediaRepository.saveAndFlush(media);

        assertEquals("collection-1", saved.getParentId());
        assertEquals("Story of Your Life", saved.getOriginalTitle());
        assertEquals(MediaType.MOVIE, saved.getMediaType());
        assertEquals(MediaStatus.ARCHIVED, saved.getStatus());
        assertEquals("A linguist is recruited to communicate with extraterrestrial visitors.", saved.getSummary());
        assertEquals(LocalDate.parse("2016-11-11"), saved.getReleaseDate());
        assertEquals(116, saved.getRuntimeMinutes());
        assertEquals("en", saved.getLanguage());
        assertEquals(0L, saved.getVersion());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
    }

    @Test
    void updateMedia_IncrementsVersion() {
        Media media = mediaRepository.saveAndFlush(new Media("media-5", "Arrival"));

        assertEquals(0L, media.getVersion());

        media.setTitle("Arrival (Updated)");
        Media updated = mediaRepository.saveAndFlush(media);

        assertEquals(1L, updated.getVersion());
    }
}
