package net.creft.lmm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.creft.lmm.dto.CreateMediaRequest;
import net.creft.lmm.dto.MediaFileRequest;
import net.creft.lmm.dto.UpdateMediaRequest;
import net.creft.lmm.exception.GlobalExceptionHandler;
import net.creft.lmm.exception.MediaNotFoundException;
import net.creft.lmm.model.Media;
import net.creft.lmm.model.MediaFile;
import net.creft.lmm.model.MediaStatus;
import net.creft.lmm.model.MediaType;
import net.creft.lmm.service.MediaDraft;
import net.creft.lmm.service.MediaFileDraft;
import net.creft.lmm.service.MediaSearchCriteria;
import net.creft.lmm.service.MediaService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MediaController.class)
@Import(GlobalExceptionHandler.class)
@ActiveProfiles("test")
public class MediaControllerTest {
    private static final LocalDate RELEASE_DATE = LocalDate.parse("2016-11-11");
    private static final Instant CREATED_AT = Instant.parse("2026-04-04T18:12:00Z");
    private static final Instant UPDATED_AT = Instant.parse("2026-04-04T18:12:30Z");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MediaService mediaService;

    @Test
    public void testListMedia_WhenPagedAndFiltered_ReturnsPageResponse() throws Exception {
        PageRequest pageRequest = PageRequest.of(1, 2, Sort.by(Sort.Order.desc("title")));
        List<Media> mediaItems = List.of(
                buildMedia("media-1", "First Title", MediaType.MOVIE, MediaStatus.ACTIVE, "en",
                        List.of(new MediaFile("/srv/media/first.mkv"))),
                buildMedia("media-2", "Second Title", MediaType.BOOK, MediaStatus.ARCHIVED, "fr",
                        List.of(new MediaFile("/srv/media/second.mkv")))
        );
        mediaItems.get(0).setParentId("collection-1");
        Mockito.when(mediaService.listMedia(
                        Mockito.eq(new MediaSearchCriteria(
                                "Title",
                                "collection-1",
                                MediaType.MOVIE,
                                MediaStatus.ACTIVE,
                                "en",
                                LocalDate.parse("2016-12-31"),
                                LocalDate.parse("2016-01-01")
                        )),
                        Mockito.any(Pageable.class)
                ))
                .thenReturn(new PageImpl<>(mediaItems, pageRequest, 5));

                mockMvc.perform(get("/media")
                        .param("title", "Title")
                        .param("parentId", "collection-1")
                        .param("mediaType", "MOVIE")
                        .param("status", "ACTIVE")
                        .param("language", "en")
                        .param("releasedBefore", "2016-12-31")
                        .param("releasedAfter", "2016-01-01")
                        .param("page", "1")
                        .param("size", "2")
                        .param("sort", "title")
                        .param("direction", "desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(org.springframework.http.MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.items[0].mediaId").value("media-1"))
                .andExpect(jsonPath("$.items[0].parentId").value("collection-1"))
                .andExpect(jsonPath("$.items[0].title").value("First Title"))
                .andExpect(jsonPath("$.items[0].mediaType").value("MOVIE"))
                .andExpect(jsonPath("$.items[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$.items[0].language").value("en"))
                .andExpect(jsonPath("$.items[0].createdAt").value(CREATED_AT.toString()))
                .andExpect(jsonPath("$.items[0].mediaFiles[0].location").value("/srv/media/first.mkv"))
                .andExpect(jsonPath("$.items[1].mediaId").value("media-2"))
                .andExpect(jsonPath("$.items[1].title").value("Second Title"))
                .andExpect(jsonPath("$.items[1].mediaType").value("BOOK"))
                .andExpect(jsonPath("$.items[1].status").value("ARCHIVED"))
                .andExpect(jsonPath("$.items[1].language").value("fr"))
                .andExpect(jsonPath("$.items[1].mediaFiles[0].location").value("/srv/media/second.mkv"))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.totalPages").value(3));

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        Mockito.verify(mediaService).listMedia(
                Mockito.eq(new MediaSearchCriteria(
                        "Title",
                        "collection-1",
                        MediaType.MOVIE,
                        MediaStatus.ACTIVE,
                        "en",
                        LocalDate.parse("2016-12-31"),
                        LocalDate.parse("2016-01-01")
                )),
                pageableCaptor.capture()
        );
        Pageable pageable = pageableCaptor.getValue();
        assertEquals(1, pageable.getPageNumber());
        assertEquals(2, pageable.getPageSize());
        assertNotNull(pageable.getSort().getOrderFor("title"));
        assertEquals(Sort.Direction.DESC, pageable.getSort().getOrderFor("title").getDirection());
    }

    @Test
    public void testListMedia_WhenPageSizeTooLarge_ReturnsValidationError() throws Exception {
        mockMvc.perform(get("/media")
                        .param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(org.springframework.http.MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.size").value("size must be between 1 and 100"));

        Mockito.verifyNoInteractions(mediaService);
    }

    @Test
    public void testListMedia_WhenSortFieldIsUnsupported_ReturnsValidationError() throws Exception {
        mockMvc.perform(get("/media")
                        .param("sort", "id"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(org.springframework.http.MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.sort")
                        .value("sort must be one of [mediaId, parentId, title, mediaType, status, releaseDate, createdAt, updatedAt]"));

        Mockito.verifyNoInteractions(mediaService);
    }

    @Test
    public void testListMedia_WhenReleaseDateRangeInvalid_ReturnsValidationError() throws Exception {
        mockMvc.perform(get("/media")
                        .param("releasedAfter", "2017-01-01")
                        .param("releasedBefore", "2016-01-01"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(org.springframework.http.MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.releasedAfter")
                        .value("releasedAfter must be on or before releasedBefore"));

        Mockito.verifyNoInteractions(mediaService);
    }

    @Test
    public void testListMedia_WhenMediaTypeFilterInvalid_UsesErrorContract() throws Exception {
        mockMvc.perform(get("/media")
                        .param("mediaType", "NOT_A_TYPE"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(org.springframework.http.MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.mediaType").value("mediaType must be a valid value"));

        Mockito.verifyNoInteractions(mediaService);
    }

    @Test
    public void testListMedia_WhenDirectionIsUnsupported_ReturnsValidationError() throws Exception {
        mockMvc.perform(get("/media")
                        .param("direction", "sideways"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(org.springframework.http.MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.direction").value("direction must be 'asc' or 'desc'"));

        Mockito.verifyNoInteractions(mediaService);
    }

    @Test
    public void testGetMedia_WhenMediaExists() throws Exception {
        String mediaId = "12345";
        Media media = buildMedia(mediaId, "Test", MediaType.MOVIE, MediaStatus.ACTIVE, "en",
                List.of(new MediaFile("/srv/media/test.mkv")));
        media.setParentId("collection-1");
        Mockito.when(mediaService.getMedia(mediaId)).thenReturn(media);

        mockMvc.perform(get("/media/{mediaId}", mediaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mediaId").value(mediaId))
                .andExpect(jsonPath("$.parentId").value("collection-1"))
                .andExpect(jsonPath("$.title").value("Test"))
                .andExpect(jsonPath("$.originalTitle").value("Original Test"))
                .andExpect(jsonPath("$.mediaType").value("MOVIE"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.summary").value("Summary for Test"))
                .andExpect(jsonPath("$.releaseDate").value(RELEASE_DATE.toString()))
                .andExpect(jsonPath("$.runtimeMinutes").value(116))
                .andExpect(jsonPath("$.language").value("en"))
                .andExpect(jsonPath("$.createdAt").value(CREATED_AT.toString()))
                .andExpect(jsonPath("$.updatedAt").value(UPDATED_AT.toString()))
                .andExpect(jsonPath("$.mediaFiles[0].location").value("/srv/media/test.mkv"));
    }

    @Test
    public void testGetMedia_WhenMediaNotFound_ReturnsErrorContract() throws Exception {
        String mediaId = "missing-id";
        Mockito.when(mediaService.getMedia(mediaId)).thenThrow(new MediaNotFoundException(mediaId));

        mockMvc.perform(get("/media/{mediaId}", mediaId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(org.springframework.http.MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Media with id 'missing-id' was not found"))
                .andExpect(jsonPath("$.fieldErrors").isMap())
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    @Test
    public void testGetMedia_WhenUnhandledException_ReturnsSanitizedError() throws Exception {
        String mediaId = "boom-id";
        Mockito.when(mediaService.getMedia(mediaId)).thenThrow(new RuntimeException("boom"));

        mockMvc.perform(get("/media/{mediaId}", mediaId))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(org.springframework.http.MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Internal server error"))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
                .andExpect(jsonPath("$.fieldErrors").isMap())
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    @Test
    public void testCreateMedia() throws Exception {
        CreateMediaRequest request = new CreateMediaRequest(
                "New Title",
                "collection-1",
                "Story of Your Life",
                MediaType.MOVIE,
                null,
                "A linguist is recruited.",
                RELEASE_DATE,
                116,
                "en",
                List.of(new MediaFileRequest(
                        "/srv/media/new-title.mkv",
                        "Main Feature",
                        "video/x-matroska",
                        7340032000L,
                        6960,
                        true
                ))
        );
        MediaDraft expectedDraft = new MediaDraft(
                "New Title",
                "Story of Your Life",
                MediaType.MOVIE,
                null,
                "A linguist is recruited.",
                RELEASE_DATE,
                116,
                "en",
                "collection-1",
                List.of(new MediaFileDraft(
                        "/srv/media/new-title.mkv",
                        "Main Feature",
                        "video/x-matroska",
                        7340032000L,
                        6960,
                        true
                ))
        );
        Media savedMedia = buildMedia(
                "generated-id",
                "New Title",
                "Story of Your Life",
                MediaType.MOVIE,
                MediaStatus.ACTIVE,
                "A linguist is recruited.",
                RELEASE_DATE,
                116,
                "en",
                List.of(new MediaFile(
                        "/srv/media/new-title.mkv",
                        "Main Feature",
                        "video/x-matroska",
                        7340032000L,
                        6960,
                        true
                ))
        );
        savedMedia.setParentId("collection-1");
        Mockito.when(mediaService.createMedia(expectedDraft)).thenReturn(savedMedia);

        mockMvc.perform(post("/media")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mediaId").value("generated-id"))
                .andExpect(jsonPath("$.parentId").value("collection-1"))
                .andExpect(jsonPath("$.title").value("New Title"))
                .andExpect(jsonPath("$.originalTitle").value("Story of Your Life"))
                .andExpect(jsonPath("$.mediaType").value("MOVIE"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.summary").value("A linguist is recruited."))
                .andExpect(jsonPath("$.releaseDate").value(RELEASE_DATE.toString()))
                .andExpect(jsonPath("$.runtimeMinutes").value(116))
                .andExpect(jsonPath("$.language").value("en"))
                .andExpect(jsonPath("$.createdAt").value(CREATED_AT.toString()))
                .andExpect(jsonPath("$.updatedAt").value(UPDATED_AT.toString()))
                .andExpect(jsonPath("$.mediaFiles[0].location").value("/srv/media/new-title.mkv"))
                .andExpect(jsonPath("$.mediaFiles[0].label").value("Main Feature"))
                .andExpect(jsonPath("$.mediaFiles[0].mimeType").value("video/x-matroska"))
                .andExpect(jsonPath("$.mediaFiles[0].sizeBytes").value(7340032000L))
                .andExpect(jsonPath("$.mediaFiles[0].durationSeconds").value(6960))
                .andExpect(jsonPath("$.mediaFiles[0].primaryFile").value(true));
    }

    @Test
    public void testCreateMedia_WhenTitleMissing_ReturnsValidationError() throws Exception {
        mockMvc.perform(post("/media")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{\"mediaType\":\"MOVIE\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(org.springframework.http.MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.title").value("title is required"));

        Mockito.verifyNoInteractions(mediaService);
    }

    @Test
    public void testCreateMedia_WhenMediaTypeMissing_ReturnsValidationError() throws Exception {
        mockMvc.perform(post("/media")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Arrival\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(org.springframework.http.MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.mediaType").value("mediaType is required"));

        Mockito.verifyNoInteractions(mediaService);
    }

    @Test
    public void testCreateMedia_WhenTitleTooLong_ReturnsValidationError() throws Exception {
        CreateMediaRequest request = new CreateMediaRequest("a".repeat(256), MediaType.MOVIE);

        mockMvc.perform(post("/media")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(org.springframework.http.MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.title").value("title must be at most 255 characters"));

        Mockito.verifyNoInteractions(mediaService);
    }

    @Test
    public void testCreateMedia_WhenRuntimeMinutesNonPositive_ReturnsValidationError() throws Exception {
        CreateMediaRequest request = new CreateMediaRequest(
                "Arrival",
                null,
                MediaType.MOVIE,
                null,
                null,
                null,
                0,
                null,
                List.of()
        );

        mockMvc.perform(post("/media")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(org.springframework.http.MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.runtimeMinutes").value("runtimeMinutes must be greater than 0"));

        Mockito.verifyNoInteractions(mediaService);
    }

    @Test
    public void testCreateMedia_WhenMediaTypeEnumInvalid_ReturnsValidationError() throws Exception {
        mockMvc.perform(post("/media")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Arrival",
                                  "mediaType": "NOT_A_TYPE"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(org.springframework.http.MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.mediaType").value("mediaType must be a valid value"));

        Mockito.verifyNoInteractions(mediaService);
    }

    @Test
    public void testCreateMedia_WhenReleaseDateInvalid_ReturnsValidationError() throws Exception {
        mockMvc.perform(post("/media")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Arrival",
                                  "mediaType": "MOVIE",
                                  "releaseDate": "11-11-2016"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(org.springframework.http.MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.releaseDate").value("releaseDate must be a valid value"));

        Mockito.verifyNoInteractions(mediaService);
    }

    @Test
    public void testCreateMedia_WhenRuntimeMinutesTypeInvalid_ReturnsValidationError() throws Exception {
        mockMvc.perform(post("/media")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Arrival",
                                  "mediaType": "MOVIE",
                                  "runtimeMinutes": "long"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(org.springframework.http.MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.runtimeMinutes").value("runtimeMinutes must be a valid value"));

        Mockito.verifyNoInteractions(mediaService);
    }

    @Test
    public void testCreateMedia_WhenMalformedJson_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/media")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"broken\""))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(org.springframework.http.MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Malformed JSON request body"))
                .andExpect(jsonPath("$.message").value("Malformed JSON request body"))
                .andExpect(jsonPath("$.fieldErrors").isMap())
                .andExpect(jsonPath("$.fieldErrors").isEmpty());

        Mockito.verifyNoInteractions(mediaService);
    }

    @Test
    public void testCreateMedia_WhenPersistenceConflict_ReturnsConflict() throws Exception {
        CreateMediaRequest request = new CreateMediaRequest("New Title", MediaType.MOVIE);
        Mockito.when(mediaService.createMedia(new MediaDraft(
                        "New Title",
                        null,
                        MediaType.MOVIE,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        List.of()
                )))
                .thenThrow(new DataIntegrityViolationException("duplicate media"));

        mockMvc.perform(post("/media")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(org.springframework.http.MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Request conflicts with existing data"))
                .andExpect(jsonPath("$.fieldErrors").isMap())
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    @Test
    public void testUpdateMedia_WhenMediaExists() throws Exception {
        String mediaId = "12345";
        UpdateMediaRequest updateRequest = new UpdateMediaRequest(
                "Updated Title",
                "collection-2",
                "Updated Original",
                MediaType.MOVIE,
                MediaStatus.ARCHIVED,
                "Updated summary",
                LocalDate.parse("2017-01-01"),
                117,
                "en-US",
                List.of(new MediaFileRequest(
                        "/srv/media/updated-title.mkv",
                        "Director Commentary",
                        "video/x-matroska",
                        1835008000L,
                        7020,
                        false
                ))
        );
        MediaDraft expectedDraft = new MediaDraft(
                "Updated Title",
                "Updated Original",
                MediaType.MOVIE,
                MediaStatus.ARCHIVED,
                "Updated summary",
                LocalDate.parse("2017-01-01"),
                117,
                "en-US",
                "collection-2",
                List.of(new MediaFileDraft(
                        "/srv/media/updated-title.mkv",
                        "Director Commentary",
                        "video/x-matroska",
                        1835008000L,
                        7020,
                        false
                ))
        );
        Media updatedMedia = buildMedia(
                mediaId,
                "Updated Title",
                "Updated Original",
                MediaType.MOVIE,
                MediaStatus.ARCHIVED,
                "Updated summary",
                LocalDate.parse("2017-01-01"),
                117,
                "en-US",
                List.of(new MediaFile(
                        "/srv/media/updated-title.mkv",
                        "Director Commentary",
                        "video/x-matroska",
                        1835008000L,
                        7020,
                        false
                ))
        );
        updatedMedia.setParentId("collection-2");
        Mockito.when(mediaService.updateMedia(mediaId, expectedDraft)).thenReturn(updatedMedia);

        mockMvc.perform(put("/media/{mediaId}", mediaId)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mediaId").value(mediaId))
                .andExpect(jsonPath("$.parentId").value("collection-2"))
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.originalTitle").value("Updated Original"))
                .andExpect(jsonPath("$.mediaType").value("MOVIE"))
                .andExpect(jsonPath("$.status").value("ARCHIVED"))
                .andExpect(jsonPath("$.summary").value("Updated summary"))
                .andExpect(jsonPath("$.releaseDate").value("2017-01-01"))
                .andExpect(jsonPath("$.runtimeMinutes").value(117))
                .andExpect(jsonPath("$.language").value("en-US"))
                .andExpect(jsonPath("$.mediaFiles[0].location").value("/srv/media/updated-title.mkv"))
                .andExpect(jsonPath("$.mediaFiles[0].label").value("Director Commentary"))
                .andExpect(jsonPath("$.mediaFiles[0].mimeType").value("video/x-matroska"))
                .andExpect(jsonPath("$.mediaFiles[0].sizeBytes").value(1835008000L))
                .andExpect(jsonPath("$.mediaFiles[0].durationSeconds").value(7020))
                .andExpect(jsonPath("$.mediaFiles[0].primaryFile").value(false));
    }

    @Test
    public void testUpdateMedia_WhenTitleBlank_ReturnsValidationError() throws Exception {
        String mediaId = "12345";
        UpdateMediaRequest updateRequest = new UpdateMediaRequest(" ", MediaType.MOVIE);

        mockMvc.perform(put("/media/{mediaId}", mediaId)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(org.springframework.http.MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.title").value("title is required"));

        Mockito.verifyNoInteractions(mediaService);
    }

    @Test
    public void testUpdateMedia_WhenMediaTypeMissing_ReturnsValidationError() throws Exception {
        mockMvc.perform(put("/media/{mediaId}", "12345")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Updated Title\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(org.springframework.http.MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.mediaType").value("mediaType is required"));

        Mockito.verifyNoInteractions(mediaService);
    }

    @Test
    public void testUpdateMedia_WhenStatusEnumInvalid_ReturnsValidationError() throws Exception {
        mockMvc.perform(put("/media/{mediaId}", "12345")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Updated Title",
                                  "mediaType": "MOVIE",
                                  "status": "NOT_A_STATUS"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(org.springframework.http.MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.status").value("status must be a valid value"));

        Mockito.verifyNoInteractions(mediaService);
    }

    @Test
    public void testUpdateMedia_WhenMediaNotFound() throws Exception {
        String mediaId = "nonexistent-id";
        UpdateMediaRequest updateRequest = new UpdateMediaRequest("Updated Title", MediaType.MOVIE);
        Mockito.when(mediaService.updateMedia(
                        mediaId,
                        new MediaDraft("Updated Title", null, MediaType.MOVIE, null, null, null, null, null, null, List.of())
                ))
                .thenThrow(new MediaNotFoundException(mediaId));

        mockMvc.perform(put("/media/{mediaId}", mediaId)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(org.springframework.http.MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Media with id 'nonexistent-id' was not found"))
                .andExpect(jsonPath("$.fieldErrors").isMap())
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    @Test
    public void testUpdateMedia_WhenMediaFileLocationBlank_ReturnsValidationError() throws Exception {
        UpdateMediaRequest updateRequest = new UpdateMediaRequest(
                "Updated Title",
                MediaType.MOVIE,
                List.of(new MediaFileRequest(" "))
        );

        mockMvc.perform(put("/media/{mediaId}", "12345")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(org.springframework.http.MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$['fieldErrors']['mediaFiles[0].location']").value("mediaFiles[].location is required"));

        Mockito.verifyNoInteractions(mediaService);
    }

    @Test
    public void testCreateMedia_WhenMultiplePrimaryFilesRequested_ReturnsValidationError() throws Exception {
        CreateMediaRequest request = new CreateMediaRequest(
                "New Title",
                MediaType.MOVIE,
                List.of(
                        new MediaFileRequest("/srv/media/new-title.mkv", "Main Feature", "video/x-matroska", 7340032000L, 6960, true),
                        new MediaFileRequest("/srv/media/new-title-alt.mkv", "Alternate Feature", "video/mp4", 2340032000L, 6960, true)
                )
        );

        mockMvc.perform(post("/media")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(org.springframework.http.MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.mediaFiles")
                        .value("mediaFiles can contain at most one primaryFile=true entry"));

        Mockito.verifyNoInteractions(mediaService);
    }

    @Test
    public void testDeleteMedia_WhenMediaExists() throws Exception {
        String mediaId = "12345";

        mockMvc.perform(delete("/media/{mediaId}", mediaId))
                .andExpect(status().isNoContent());

        Mockito.verify(mediaService, Mockito.times(1)).deleteMedia(mediaId);
    }

    @Test
    public void testDeleteMedia_WhenMediaNotFound() throws Exception {
        String mediaId = "nonexistent-id";
        Mockito.doThrow(new MediaNotFoundException(mediaId)).when(mediaService).deleteMedia(mediaId);

        mockMvc.perform(delete("/media/{mediaId}", mediaId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(org.springframework.http.MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Media with id 'nonexistent-id' was not found"))
                .andExpect(jsonPath("$.fieldErrors").isMap())
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    @Test
    public void testDeleteMedia_WhenPageParameterHasWrongType_UsesErrorContract() throws Exception {
        mockMvc.perform(get("/media")
                        .param("page", "oops"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(org.springframework.http.MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.page").value("page must be a valid value"));

        Mockito.verifyNoInteractions(mediaService);
    }

    private Media buildMedia(
            String mediaId,
            String title,
            MediaType mediaType,
            MediaStatus status,
            String language,
            List<MediaFile> mediaFiles
    ) {
        return buildMedia(
                mediaId,
                title,
                "Original " + title,
                mediaType,
                status,
                "Summary for " + title,
                RELEASE_DATE,
                116,
                language,
                mediaFiles
        );
    }

    private Media buildMedia(
            String mediaId,
            String title,
            String originalTitle,
            MediaType mediaType,
            MediaStatus status,
            String summary,
            LocalDate releaseDate,
            Integer runtimeMinutes,
            String language,
            List<MediaFile> mediaFiles
    ) {
        Media media = new Media(mediaId, title, mediaFiles);
        media.setOriginalTitle(originalTitle);
        media.setMediaType(mediaType);
        media.setStatus(status);
        media.setSummary(summary);
        media.setReleaseDate(releaseDate);
        media.setRuntimeMinutes(runtimeMinutes);
        media.setLanguage(language);
        media.setCreatedAt(CREATED_AT);
        media.setUpdatedAt(UPDATED_AT);
        return media;
    }
}
