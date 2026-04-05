package net.creft.lmm.service;

import net.creft.lmm.exception.InvalidRequestParameterException;
import net.creft.lmm.exception.MediaNotFoundException;
import net.creft.lmm.model.Media;
import net.creft.lmm.model.MediaFile;
import net.creft.lmm.model.MediaStatus;
import net.creft.lmm.model.MediaType;
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
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MediaServiceImplTest {
    private static final LocalDate RELEASE_DATE = LocalDate.parse("2016-11-11");

    @Mock
    private MediaRepository mediaRepository;

    @InjectMocks
    private MediaServiceImpl mediaService;

    @Test
    void listMedia_WithoutFilter_UsesFindAll() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Media> page = new PageImpl<>(List.of(new Media("media-1", "Title")), pageable, 1);
        when(mediaRepository.findAll(org.mockito.Mockito.<Specification<Media>>any(), org.mockito.Mockito.eq(pageable)))
                .thenReturn(page);

        Page<Media> result = mediaService.listMedia(
                new MediaSearchCriteria(null, null, null, null, null, null, null),
                pageable
        );

        assertEquals(page, result);
        verify(mediaRepository).findAll(org.mockito.Mockito.<Specification<Media>>any(), org.mockito.Mockito.eq(pageable));
    }

    @Test
    void listMedia_WithBlankFilter_UsesFindAll() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Media> page = new PageImpl<>(List.of(new Media("media-1", "Title")), pageable, 1);
        when(mediaRepository.findAll(org.mockito.Mockito.<Specification<Media>>any(), org.mockito.Mockito.eq(pageable)))
                .thenReturn(page);

        Page<Media> result = mediaService.listMedia(
                new MediaSearchCriteria("   ", "   ", null, null, "   ", null, null),
                pageable
        );

        assertEquals(page, result);
        verify(mediaRepository).findAll(org.mockito.Mockito.<Specification<Media>>any(), org.mockito.Mockito.eq(pageable));
    }

    @Test
    void listMedia_WithRichFilters_UsesSpecificationQuery() {
        Pageable pageable = PageRequest.of(1, 5);
        Page<Media> page = new PageImpl<>(List.of(new Media("media-1", "Filtered Title")), pageable, 1);
        when(mediaRepository.findAll(org.mockito.Mockito.<Specification<Media>>any(), org.mockito.Mockito.eq(pageable)))
                .thenReturn(page);

        Page<Media> result = mediaService.listMedia(
                new MediaSearchCriteria(
                        " Filtered ",
                        " collection-1 ",
                        MediaType.MOVIE,
                        MediaStatus.ACTIVE,
                        " en ",
                        LocalDate.parse("2016-12-31"),
                        LocalDate.parse("2016-01-01")
                ),
                pageable
        );

        assertEquals(page, result);
        verify(mediaRepository).findAll(org.mockito.Mockito.<Specification<Media>>any(), org.mockito.Mockito.eq(pageable));
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
    void createMedia_PersistsGeneratedMediaIdTrimmedMetadataAndDefaults() {
        ArgumentCaptor<Media> captor = ArgumentCaptor.forClass(Media.class);
        when(mediaRepository.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));
        when(mediaRepository.findByMediaId("collection-1")).thenReturn(new Media("collection-1", "Collection"));

        Media result = mediaService.createMedia(new MediaDraft(
                "  New Title  ",
                "  Story of Your Life  ",
                MediaType.MOVIE,
                null,
                "  A linguist is recruited.  ",
                RELEASE_DATE,
                116,
                " en ",
                "  collection-1  ",
                List.of(new MediaFileDraft(
                        "/srv/media/new-title.mkv",
                        "  Main Feature  ",
                        "  video/x-matroska  ",
                        7340032000L,
                        6960,
                        true
                ))
        ));

        verify(mediaRepository).save(captor.getValue());
        assertEquals("New Title", result.getTitle());
        assertEquals("Story of Your Life", result.getOriginalTitle());
        assertEquals(MediaType.MOVIE, result.getMediaType());
        assertEquals(MediaStatus.ACTIVE, result.getStatus());
        assertEquals("A linguist is recruited.", result.getSummary());
        assertEquals(RELEASE_DATE, result.getReleaseDate());
        assertEquals(116, result.getRuntimeMinutes());
        assertEquals("en", result.getLanguage());
        assertEquals("collection-1", result.getParentId());
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
    void updateMedia_WhenMediaExists_UpdatesRichMetadataAndReturnsSavedMedia() {
        Media existing = new Media("media-1", "Old", List.of(new MediaFile("/srv/media/old.mkv")));
        existing.setMediaType(MediaType.MOVIE);
        existing.setStatus(MediaStatus.ACTIVE);
        when(mediaRepository.findByMediaId("media-1")).thenReturn(existing);
        when(mediaRepository.findByMediaId("collection-2")).thenReturn(new Media("collection-2", "Collection"));
        when(mediaRepository.save(existing)).thenReturn(existing);

        Media updated = mediaService.updateMedia("media-1", new MediaDraft(
                "  Updated  ",
                "  Updated Original  ",
                MediaType.SERIES,
                MediaStatus.ARCHIVED,
                "  Updated summary  ",
                LocalDate.parse("2020-01-01"),
                120,
                " en-US ",
                "  collection-2  ",
                List.of(new MediaFileDraft(
                        "/srv/media/updated.mkv",
                        " 4K Remux ",
                        " video/x-matroska ",
                        9340032000L,
                        6990,
                        true
                ))
        ));

        assertEquals("media-1", updated.getMediaId());
        assertEquals("Updated", updated.getTitle());
        assertEquals("Updated Original", updated.getOriginalTitle());
        assertEquals(MediaType.SERIES, updated.getMediaType());
        assertEquals(MediaStatus.ARCHIVED, updated.getStatus());
        assertEquals("Updated summary", updated.getSummary());
        assertEquals(LocalDate.parse("2020-01-01"), updated.getReleaseDate());
        assertEquals(120, updated.getRuntimeMinutes());
        assertEquals("en-US", updated.getLanguage());
        assertEquals("collection-2", updated.getParentId());
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
    void updateMedia_NormalizesOptionalStringsToNull() {
        Media existing = new Media("media-1", "Old", List.of(new MediaFile("/srv/media/old.mkv")));
        existing.setMediaType(MediaType.MOVIE);
        existing.setStatus(MediaStatus.ARCHIVED);
        when(mediaRepository.findByMediaId("media-1")).thenReturn(existing);
        when(mediaRepository.save(existing)).thenReturn(existing);

        Media updated = mediaService.updateMedia("media-1", new MediaDraft(
                "  Updated  ",
                "   ",
                MediaType.MOVIE,
                null,
                "   ",
                RELEASE_DATE,
                null,
                "   ",
                "   ",
                List.of(new MediaFileDraft(
                        "/srv/media/updated.mkv",
                        "   ",
                        "   ",
                        null,
                        null,
                        false
                ))
        ));

        assertEquals("media-1", updated.getMediaId());
        assertEquals("Updated", updated.getTitle());
        assertNull(updated.getOriginalTitle());
        assertEquals(MediaType.MOVIE, updated.getMediaType());
        assertEquals(MediaStatus.ACTIVE, updated.getStatus());
        assertNull(updated.getSummary());
        assertNull(updated.getRuntimeMinutes());
        assertNull(updated.getLanguage());
        assertNull(updated.getParentId());
        assertEquals("/srv/media/updated.mkv", updated.getMediaFiles().get(0).getLocation());
        assertNull(updated.getMediaFiles().get(0).getLabel());
        assertNull(updated.getMediaFiles().get(0).getMimeType());
        verify(mediaRepository).save(existing);
    }

    @Test
    void updateMedia_WhenMediaDoesNotExist_ThrowsNotFound() {
        when(mediaRepository.findByMediaId("missing")).thenReturn(null);

        assertThrows(MediaNotFoundException.class, () -> mediaService.updateMedia("missing", minimalDraft("Updated")));
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

        Media result = mediaService.createMedia(new MediaDraft(
                "New Title",
                "   ",
                MediaType.MOVIE,
                null,
                "   ",
                RELEASE_DATE,
                116,
                "   ",
                "   ",
                List.of(new MediaFileDraft(
                        "/srv/media/new-title.mkv",
                        "   ",
                        "   ",
                        7340032000L,
                        6960,
                        false
                ))
        ));

        assertNull(result.getOriginalTitle());
        assertNull(result.getSummary());
        assertNull(result.getLanguage());
        assertNull(result.getParentId());
        assertNull(result.getMediaFiles().get(0).getLabel());
        assertNull(result.getMediaFiles().get(0).getMimeType());
    }

    @Test
    void createMedia_WhenParentIdDoesNotExist_ThrowsValidationError() {
        when(mediaRepository.findByMediaId("missing-parent")).thenReturn(null);

        InvalidRequestParameterException exception = assertThrows(
                InvalidRequestParameterException.class,
                () -> mediaService.createMedia(new MediaDraft(
                        "New Title",
                        null,
                        MediaType.MOVIE,
                        null,
                        null,
                        null,
                        null,
                        null,
                        "missing-parent",
                        List.of()
                ))
        );

        assertEquals("parentId must reference an existing mediaId", exception.getMessage());
        verify(mediaRepository, never()).save(org.mockito.ArgumentMatchers.any(Media.class));
    }

    @Test
    void updateMedia_WhenParentIdReferencesSelf_ThrowsValidationError() {
        Media existing = new Media("media-1", "Old");
        existing.setMediaType(MediaType.MOVIE);
        when(mediaRepository.findByMediaId("media-1")).thenReturn(existing);

        InvalidRequestParameterException exception = assertThrows(
                InvalidRequestParameterException.class,
                () -> mediaService.updateMedia("media-1", new MediaDraft(
                        "Updated",
                        null,
                        MediaType.MOVIE,
                        null,
                        null,
                        null,
                        null,
                        null,
                        "media-1",
                        List.of()
                ))
        );

        assertEquals("parentId cannot reference the same media item", exception.getMessage());
        verify(mediaRepository, never()).save(org.mockito.ArgumentMatchers.any(Media.class));
    }

    @Test
    void updateMedia_WhenParentIdCreatesCycle_ThrowsValidationError() {
        Media existing = new Media("media-1", "Old");
        existing.setMediaType(MediaType.MOVIE);
        Media parent = new Media("collection-1", "Collection");
        parent.setParentId("media-1");
        when(mediaRepository.findByMediaId("media-1")).thenReturn(existing);
        when(mediaRepository.findByMediaId("collection-1")).thenReturn(parent);

        InvalidRequestParameterException exception = assertThrows(
                InvalidRequestParameterException.class,
                () -> mediaService.updateMedia("media-1", new MediaDraft(
                        "Updated",
                        null,
                        MediaType.MOVIE,
                        null,
                        null,
                        null,
                        null,
                        null,
                        "collection-1",
                        List.of()
                ))
        );

        assertEquals("parentId cannot create a cycle", exception.getMessage());
        verify(mediaRepository, never()).save(org.mockito.ArgumentMatchers.any(Media.class));
    }

    @Test
    void createMedia_WhenMultiplePrimaryFilesExist_ThrowsValidationError() {
        assertThrows(
                InvalidRequestParameterException.class,
                () -> mediaService.createMedia(new MediaDraft(
                        "New Title",
                        null,
                        MediaType.MOVIE,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        List.of(
                                new MediaFileDraft("/srv/media/new-title.mkv", "Main Feature", "video/x-matroska", 7340032000L, 6960, true),
                                new MediaFileDraft("/srv/media/new-title-alt.mkv", "Alternate Feature", "video/mp4", 2340032000L, 6960, true)
                        )
                ))
        );

        verify(mediaRepository, never()).save(org.mockito.ArgumentMatchers.any(Media.class));
    }

    @Test
    void createMedia_WhenMediaTypeMissing_ThrowsValidationError() {
        assertThrows(
                InvalidRequestParameterException.class,
                () -> mediaService.createMedia(new MediaDraft(
                        "New Title",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        List.of()
                ))
        );

        verify(mediaRepository, never()).save(org.mockito.ArgumentMatchers.any(Media.class));
    }

    @Test
    void createMedia_WhenTitleBlank_ThrowsValidationError() {
        assertThrows(
                InvalidRequestParameterException.class,
                () -> mediaService.createMedia(new MediaDraft(
                        "   ",
                        null,
                        MediaType.MOVIE,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        List.of()
                ))
        );

        verify(mediaRepository, never()).save(org.mockito.ArgumentMatchers.any(Media.class));
    }

    private MediaDraft minimalDraft(String title) {
        return new MediaDraft(title, null, MediaType.MOVIE, null, null, null, null, null, null, List.of());
    }
}
