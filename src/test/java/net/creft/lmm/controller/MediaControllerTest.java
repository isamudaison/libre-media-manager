package net.creft.lmm.controller;

import net.creft.lmm.dto.CreateMediaRequest;
import net.creft.lmm.dto.UpdateMediaRequest;
import net.creft.lmm.model.Media;
import net.creft.lmm.repository.MediaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MediaController.class)
public class MediaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;  // Provided by Spring Boot's test starter

    // We need to provide a mock for the MediaRepository dependency.
    @MockBean
    private MediaRepository mediaRepository;

    @Test
    public void testGetMedia_WhenMediaExists() throws Exception {
        // Arrange: set up your test data and expectations
        String mediaId = "12345";
        Media media = new Media(mediaId, "Test");
        Mockito.when(mediaRepository.findByMediaId(mediaId)).thenReturn(media);

        // Act & Assert: perform the GET request and verify the result
        mockMvc.perform(get("/media/{mediaId}", mediaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mediaId").value(mediaId))
                .andExpect(jsonPath("$.title").value("Test"));
    }

    @Test
    public void testCreateMedia() throws Exception {
        // Arrange: prepare the request and expected result.
        CreateMediaRequest request = new CreateMediaRequest("New Title");
        // Since the controller generates the media ID using UUID, we simulate repository behavior.
        Media savedMedia = new Media("generated-id", request.getTitle());
        Mockito.when(mediaRepository.save(Mockito.any(Media.class))).thenReturn(savedMedia);

        // Act & Assert: perform the POST request and check the response.
        mockMvc.perform(post("/media")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mediaId").value("generated-id"))
                .andExpect(jsonPath("$.title").value("New Title"));
    }

    @Test
    public void testUpdateMedia_WhenMediaExists() throws Exception {
        // Arrange: set up an existing media with an old title.
        String mediaId = "12345";
        Media existingMedia = new Media(mediaId, "Old Title");
        Mockito.when(mediaRepository.findByMediaId(mediaId)).thenReturn(existingMedia);

        // Create an update request with the new title.
        UpdateMediaRequest updateRequest = new UpdateMediaRequest("Updated Title");

        // Simulate the repository saving the media with the updated title.
        Media updatedMedia = new Media(mediaId, "Updated Title");
        Mockito.when(mediaRepository.save(Mockito.any(Media.class))).thenReturn(updatedMedia);

        // Act & Assert: perform the PUT request and verify that the updated media is returned.
        mockMvc.perform(put("/media/{mediaId}", mediaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mediaId").value(mediaId))
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    @Test
    public void testUpdateMedia_WhenMediaNotFound() throws Exception {
        // Arrange: simulate that the media is not found.
        String mediaId = "nonexistent-id";
        Mockito.when(mediaRepository.findByMediaId(mediaId)).thenReturn(null);

        UpdateMediaRequest updateRequest = new UpdateMediaRequest("Updated Title");

        // Act & Assert: perform the PUT request and expect a 404 Not Found status.
        mockMvc.perform(put("/media/{mediaId}", mediaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testDeleteMedia_WhenMediaExists() throws Exception {
        // Arrange: Set up an existing media object.
        String mediaId = "12345";
        Media existingMedia = new Media(mediaId, "Test");
        Mockito.when(mediaRepository.findByMediaId(mediaId)).thenReturn(existingMedia);

        // Act & Assert: Perform the DELETE request and expect a 204 No Content response.
        mockMvc.perform(delete("/media/{mediaId}", mediaId))
                .andExpect(status().isNoContent());

        // Verify that the repository's delete method was invoked.
        Mockito.verify(mediaRepository, Mockito.times(1)).delete(existingMedia);
    }

    @Test
    public void testDeleteMedia_WhenMediaNotFound() throws Exception {
        // Arrange: Simulate that no media exists with the given ID.
        String mediaId = "nonexistent-id";
        Mockito.when(mediaRepository.findByMediaId(mediaId)).thenReturn(null);

        // Act & Assert: Perform the DELETE request and expect a 404 Not Found response.
        mockMvc.perform(delete("/media/{mediaId}", mediaId))
                .andExpect(status().isNotFound());
    }

}
