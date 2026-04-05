package net.creft.lmm.repository;

import net.creft.lmm.model.Media;
import net.creft.lmm.model.MediaFile;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest(showSql = false)
@ActiveProfiles("test")
class MediaRepositoryTest {

    @Autowired
    private MediaRepository mediaRepository;

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
    void saveMediaWithFiles_PersistsOrderedChildRows() {
        Media media = new Media(
                "media-3",
                "Arrival",
                List.of(
                        new MediaFile(
                                "/srv/media/arrival-1080p.mkv",
                                "1080p Encode",
                                "video/x-matroska",
                                4831838208L,
                                6960,
                                false
                        ),
                        new MediaFile(
                                "/srv/media/arrival-4k.mkv",
                                "4K Remux",
                                "video/x-matroska",
                                16831838208L,
                                6960,
                                true
                        )
                )
        );

        Media saved = mediaRepository.saveAndFlush(media);

        assertEquals(2, saved.getMediaFiles().size());
        assertEquals("/srv/media/arrival-1080p.mkv", saved.getMediaFiles().get(0).getLocation());
        assertEquals("/srv/media/arrival-4k.mkv", saved.getMediaFiles().get(1).getLocation());
        assertEquals("1080p Encode", saved.getMediaFiles().get(0).getLabel());
        assertEquals("video/x-matroska", saved.getMediaFiles().get(0).getMimeType());
        assertEquals(4831838208L, saved.getMediaFiles().get(0).getSizeBytes());
        assertEquals(6960, saved.getMediaFiles().get(0).getDurationSeconds());
        assertEquals(false, saved.getMediaFiles().get(0).isPrimaryFile());
        assertEquals("4K Remux", saved.getMediaFiles().get(1).getLabel());
        assertEquals(true, saved.getMediaFiles().get(1).isPrimaryFile());
    }
}
