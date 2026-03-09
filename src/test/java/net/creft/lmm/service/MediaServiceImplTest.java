package net.creft.lmm.service;

import net.creft.lmm.exception.MediaNotFoundException;
import net.creft.lmm.model.Media;
import net.creft.lmm.repository.MediaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MediaServiceImplTest {

    @Mock
    private MediaRepository mediaRepository;

    @InjectMocks
    private MediaServiceImpl mediaService;

    @Test
    void getMedia_WhenMediaExists_ReturnsMedia() {
        Media media = new Media("media-1", "Title");
        when(mediaRepository.findByMediaId("media-1")).thenReturn(media);

        Media result = mediaService.getMedia("media-1");

        assertEquals("media-1", result.getMediaId());
        assertEquals("Title", result.getTitle());
    }

    @Test
    void getMedia_WhenMediaDoesNotExist_ThrowsNotFound() {
        when(mediaRepository.findByMediaId("missing")).thenReturn(null);

        assertThrows(MediaNotFoundException.class, () -> mediaService.getMedia("missing"));
    }

    @Test
    void createMedia_PersistsGeneratedMediaIdAndTitle() {
        ArgumentCaptor<Media> captor = ArgumentCaptor.forClass(Media.class);
        when(mediaRepository.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        Media result = mediaService.createMedia("New Title");

        verify(mediaRepository).save(captor.getValue());
        assertEquals("New Title", result.getTitle());
        assertNotNull(result.getMediaId());
        UUID.fromString(result.getMediaId());
    }

    @Test
    void updateMedia_WhenMediaExists_UpdatesTitleAndReturnsSavedMedia() {
        Media existing = new Media("media-1", "Old");
        when(mediaRepository.findByMediaId("media-1")).thenReturn(existing);
        when(mediaRepository.save(existing)).thenReturn(existing);

        Media updated = mediaService.updateMedia("media-1", "Updated");

        assertEquals("Updated", updated.getTitle());
        verify(mediaRepository).save(existing);
    }

    @Test
    void updateMedia_WhenMediaDoesNotExist_ThrowsNotFound() {
        when(mediaRepository.findByMediaId("missing")).thenReturn(null);

        assertThrows(MediaNotFoundException.class, () -> mediaService.updateMedia("missing", "Updated"));
        verify(mediaRepository, never()).save(org.mockito.ArgumentMatchers.any(Media.class));
    }

    @Test
    void deleteMedia_WhenMediaExists_DeletesMedia() {
        Media existing = new Media("media-1", "Title");
        when(mediaRepository.findByMediaId("media-1")).thenReturn(existing);
        doNothing().when(mediaRepository).delete(existing);

        mediaService.deleteMedia("media-1");

        verify(mediaRepository).delete(existing);
    }

    @Test
    void deleteMedia_WhenMediaDoesNotExist_ThrowsNotFound() {
        when(mediaRepository.findByMediaId("missing")).thenReturn(null);

        assertThrows(MediaNotFoundException.class, () -> mediaService.deleteMedia("missing"));
        verify(mediaRepository, never()).delete(org.mockito.ArgumentMatchers.any(Media.class));
    }
}
