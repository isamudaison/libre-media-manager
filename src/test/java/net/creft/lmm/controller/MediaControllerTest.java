package net.creft.lmm.controller;

import net.creft.lmm.dto.CreateMediaRequest;
import net.creft.lmm.dto.UpdateMediaRequest;
import net.creft.lmm.exception.GlobalExceptionHandler;
import net.creft.lmm.exception.MediaNotFoundException;
import net.creft.lmm.model.Media;
import net.creft.lmm.service.MediaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.mockito.ArgumentCaptor;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MediaController.class)
@Import(GlobalExceptionHandler.class)
@ActiveProfiles("test")
public class MediaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;  // Provided by Spring Boot's test starter

    @MockitoBean
    private MediaService mediaService;

    @Test
    public void testListMedia_WhenPagedAndFiltered_ReturnsPageResponse() throws Exception {
        PageRequest pageRequest = PageRequest.of(1, 2, Sort.by(Sort.Order.desc("title")));
        List<Media> mediaItems = List.of(
                new Media("media-1", "First Title"),
                new Media("media-2", "Second Title")
        );
        Mockito.when(mediaService.listMedia(Mockito.eq("Title"), Mockito.any(Pageable.class)))
                .thenReturn(new PageImpl<>(mediaItems, pageRequest, 5));

        mockMvc.perform(get("/media")
                        .param("title", "Title")
                        .param("page", "1")
                        .param("size", "2")
                        .param("sort", "title")
                        .param("direction", "desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.items[0].mediaId").value("media-1"))
                .andExpect(jsonPath("$.items[0].title").value("First Title"))
                .andExpect(jsonPath("$.items[1].mediaId").value("media-2"))
                .andExpect(jsonPath("$.items[1].title").value("Second Title"))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.totalPages").value(3));

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        Mockito.verify(mediaService).listMedia(Mockito.eq("Title"), pageableCaptor.capture());
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
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
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
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.sort").value("sort must be one of [mediaId, title]"));

        Mockito.verifyNoInteractions(mediaService);
    }

    @Test
    public void testListMedia_WhenDirectionIsUnsupported_ReturnsValidationError() throws Exception {
        mockMvc.perform(get("/media")
                        .param("direction", "sideways"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.direction").value("direction must be 'asc' or 'desc'"));

        Mockito.verifyNoInteractions(mediaService);
    }

    @Test
    public void testGetMedia_WhenMediaExists() throws Exception {
        String mediaId = "12345";
        Media media = new Media(mediaId, "Test");
        Mockito.when(mediaService.getMedia(mediaId)).thenReturn(media);

        mockMvc.perform(get("/media/{mediaId}", mediaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mediaId").value(mediaId))
                .andExpect(jsonPath("$.title").value("Test"));
    }

    @Test
    public void testGetMedia_WhenMediaNotFound_ReturnsErrorContract() throws Exception {
        String mediaId = "missing-id";
        Mockito.when(mediaService.getMedia(mediaId)).thenThrow(new MediaNotFoundException(mediaId));

        mockMvc.perform(get("/media/{mediaId}", mediaId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
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
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Internal server error"))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
                .andExpect(jsonPath("$.fieldErrors").isMap())
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    @Test
    public void testCreateMedia() throws Exception {
        CreateMediaRequest request = new CreateMediaRequest("New Title");
        Media savedMedia = new Media("generated-id", request.getTitle());
        Mockito.when(mediaService.createMedia(request.getTitle())).thenReturn(savedMedia);

        mockMvc.perform(post("/media")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mediaId").value("generated-id"))
                .andExpect(jsonPath("$.title").value("New Title"));
    }

    @Test
    public void testCreateMedia_WhenTitleMissing_ReturnsValidationError() throws Exception {
        mockMvc.perform(post("/media")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.title").value("title is required"));

        Mockito.verifyNoInteractions(mediaService);
    }

    @Test
    public void testCreateMedia_WhenTitleTooLong_ReturnsValidationError() throws Exception {
        CreateMediaRequest request = new CreateMediaRequest("a".repeat(256));

        mockMvc.perform(post("/media")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.title").value("title must be at most 255 characters"));

        Mockito.verifyNoInteractions(mediaService);
    }

    @Test
    public void testCreateMedia_WhenMalformedJson_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/media")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"broken\""))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Malformed JSON request body"))
                .andExpect(jsonPath("$.message").value("Malformed JSON request body"))
                .andExpect(jsonPath("$.fieldErrors").isMap())
                .andExpect(jsonPath("$.fieldErrors").isEmpty());

        Mockito.verifyNoInteractions(mediaService);
    }

    @Test
    public void testCreateMedia_WhenPersistenceConflict_ReturnsConflict() throws Exception {
        CreateMediaRequest request = new CreateMediaRequest("New Title");
        Mockito.when(mediaService.createMedia(request.getTitle()))
                .thenThrow(new DataIntegrityViolationException("duplicate media"));

        mockMvc.perform(post("/media")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Request conflicts with existing data"))
                .andExpect(jsonPath("$.fieldErrors").isMap())
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    @Test
    public void testUpdateMedia_WhenMediaExists() throws Exception {
        String mediaId = "12345";
        UpdateMediaRequest updateRequest = new UpdateMediaRequest("Updated Title");
        Media updatedMedia = new Media(mediaId, "Updated Title");
        Mockito.when(mediaService.updateMedia(mediaId, "Updated Title")).thenReturn(updatedMedia);

        mockMvc.perform(put("/media/{mediaId}", mediaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mediaId").value(mediaId))
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    @Test
    public void testUpdateMedia_WhenTitleBlank_ReturnsValidationError() throws Exception {
        String mediaId = "12345";
        UpdateMediaRequest updateRequest = new UpdateMediaRequest(" ");

        mockMvc.perform(put("/media/{mediaId}", mediaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.title").value("title is required"));

        Mockito.verifyNoInteractions(mediaService);
    }

    @Test
    public void testUpdateMedia_WhenMediaNotFound() throws Exception {
        String mediaId = "nonexistent-id";
        Mockito.when(mediaService.updateMedia(mediaId, "Updated Title")).thenThrow(new MediaNotFoundException(mediaId));
        UpdateMediaRequest updateRequest = new UpdateMediaRequest("Updated Title");

        mockMvc.perform(put("/media/{mediaId}", mediaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Media with id 'nonexistent-id' was not found"))
                .andExpect(jsonPath("$.fieldErrors").isMap())
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
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
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
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
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.page").value("page must be a valid value"));

        Mockito.verifyNoInteractions(mediaService);
    }

}
