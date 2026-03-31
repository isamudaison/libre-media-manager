package net.creft.lmm.repository;

import net.creft.lmm.model.Media;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

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
}
