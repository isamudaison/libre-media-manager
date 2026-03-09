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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MediaController.class)
@Import(GlobalExceptionHandler.class)
public class MediaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;  // Provided by Spring Boot's test starter

    @MockBean
    private MediaService mediaService;

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

}
