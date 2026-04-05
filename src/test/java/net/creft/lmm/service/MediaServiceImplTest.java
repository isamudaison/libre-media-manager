package net.creft.lmm.service;

import net.creft.lmm.exception.MediaNotFoundException;
import net.creft.lmm.model.Media;
import net.creft.lmm.model.MediaFile;
import net.creft.lmm.repository.MediaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNull;
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
    void listMedia_WithoutFilter_UsesFindAll() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Media> page = new PageImpl<>(List.of(new Media("media-1", "Title")), pageable, 1);
        when(mediaRepository.findAll(pageable)).thenReturn(page);

        Page<Media> result = mediaService.listMedia(null, pageable);

        assertEquals(page, result);
        verify(mediaRepository).findAll(pageable);
        verify(mediaRepository, never()).findByTitleContainingIgnoreCase(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any(Pageable.class));
    }

    @Test
    void listMedia_WithBlankFilter_UsesFindAll() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Media> page = new PageImpl<>(List.of(new Media("media-1", "Title")), pageable, 1);
        when(mediaRepository.findAll(pageable)).thenReturn(page);

        Page<Media> result = mediaService.listMedia("   ", pageable);

        assertEquals(page, result);
        verify(mediaRepository).findAll(pageable);
        verify(mediaRepository, never()).findByTitleContainingIgnoreCase(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any(Pageable.class));
    }

    @Test
    void listMedia_WithFilter_UsesContainingIgnoreCaseQuery() {
        Pageable pageable = PageRequest.of(1, 5);
        Page<Media> page = new PageImpl<>(List.of(new Media("media-1", "Filtered Title")), pageable, 1);
        when(mediaRepository.findByTitleContainingIgnoreCase("Filtered", pageable)).thenReturn(page);

        Page<Media> result = mediaService.listMedia(" Filtered ", pageable);

        assertEquals(page, result);
        verify(mediaRepository).findByTitleContainingIgnoreCase("Filtered", pageable);
        verify(mediaRepository, never()).findAll(pageable);
    }

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

        Media result = mediaService.createMedia(
                "New Title",
                List.of(new MediaFileDraft(
                        "/srv/media/new-title.mkv",
                        "Main Feature",
                        "video/x-matroska",
                        7340032000L,
                        6960,
                        true
                ))
        );

        verify(mediaRepository).save(captor.getValue());
        assertEquals("New Title", result.getTitle());
        assertNotNull(result.getMediaId());
        assertEquals(1, result.getMediaFiles().size());
        assertEquals("/srv/media/new-title.mkv", result.getMediaFiles().get(0).getLocation());
        assertEquals("Main Feature", result.getMediaFiles().get(0).getLabel());
        assertEquals("video/x-matroska", result.getMediaFiles().get(0).getMimeType());
        assertEquals(7340032000L, result.getMediaFiles().get(0).getSizeBytes());
        assertEquals(6960, result.getMediaFiles().get(0).getDurationSeconds());
        assertEquals(true, result.getMediaFiles().get(0).isPrimaryFile());
        UUID.fromString(result.getMediaId());
    }

    @Test
    void updateMedia_WhenMediaExists_UpdatesTitleAndReturnsSavedMedia() {
        Media existing = new Media("media-1", "Old", List.of(new MediaFile("/srv/media/old.mkv")));
        when(mediaRepository.findByMediaId("media-1")).thenReturn(existing);
        when(mediaRepository.save(existing)).thenReturn(existing);

        Media updated = mediaService.updateMedia(
                "media-1",
                "Updated",
                List.of(new MediaFileDraft(
                        "/srv/media/updated.mkv",
                        "4K Remux",
                        "video/x-matroska",
                        9340032000L,
                        6990,
                        true
                ))
        );

        assertEquals("Updated", updated.getTitle());
        assertEquals(1, updated.getMediaFiles().size());
        assertEquals("/srv/media/updated.mkv", updated.getMediaFiles().get(0).getLocation());
        assertEquals("4K Remux", updated.getMediaFiles().get(0).getLabel());
        assertEquals("video/x-matroska", updated.getMediaFiles().get(0).getMimeType());
        assertEquals(9340032000L, updated.getMediaFiles().get(0).getSizeBytes());
        assertEquals(6990, updated.getMediaFiles().get(0).getDurationSeconds());
        assertEquals(true, updated.getMediaFiles().get(0).isPrimaryFile());
        verify(mediaRepository).save(existing);
    }

    @Test
    void updateMedia_WhenMediaDoesNotExist_ThrowsNotFound() {
        when(mediaRepository.findByMediaId("missing")).thenReturn(null);

        assertThrows(MediaNotFoundException.class, () -> mediaService.updateMedia("missing", "Updated", List.of()));
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

    @Test
    void createMedia_NormalizesOptionalStringsToNull() {
        ArgumentCaptor<Media> captor = ArgumentCaptor.forClass(Media.class);
        when(mediaRepository.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        Media result = mediaService.createMedia(
                "New Title",
                List.of(new MediaFileDraft(
                        "/srv/media/new-title.mkv",
                        "   ",
                        "   ",
                        7340032000L,
                        6960,
                        false
                ))
        );

        assertNull(result.getMediaFiles().get(0).getLabel());
        assertNull(result.getMediaFiles().get(0).getMimeType());
    }

    @Test
    void createMedia_WhenMultiplePrimaryFilesExist_ThrowsValidationError() {
        assertThrows(
                net.creft.lmm.exception.InvalidRequestParameterException.class,
                () -> mediaService.createMedia(
                        "New Title",
                        List.of(
                                new MediaFileDraft("/srv/media/new-title.mkv", "Main Feature", "video/x-matroska", 7340032000L, 6960, true),
                                new MediaFileDraft("/srv/media/new-title-alt.mkv", "Alternate Feature", "video/mp4", 2340032000L, 6960, true)
                        )
                )
        );

        verify(mediaRepository, never()).save(org.mockito.ArgumentMatchers.any(Media.class));
    }
}
