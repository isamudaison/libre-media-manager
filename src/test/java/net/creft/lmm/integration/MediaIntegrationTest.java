package net.creft.lmm.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.creft.lmm.model.Media;
import net.creft.lmm.repository.MediaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.matchesPattern;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MediaIntegrationTest {
    private static final String REQUEST_ID_HEADER = "X-Request-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MediaRepository mediaRepository;

    @BeforeEach
    void setUp() {
        mediaRepository.deleteAll();
    }

    @Test
    void createGetUpdateDeleteMedia_EndToEnd() throws Exception {
        String createResponseBody = mockMvc.perform(post("/media")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Arrival"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.mediaId").isString())
                .andExpect(jsonPath("$.title").value("Arrival"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode createdMedia = objectMapper.readTree(createResponseBody);
        String mediaId = createdMedia.path("mediaId").asText();

        mockMvc.perform(get("/media/{mediaId}", mediaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mediaId").value(mediaId))
                .andExpect(jsonPath("$.title").value("Arrival"));

        mockMvc.perform(put("/media/{mediaId}", mediaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Arrival (Updated)"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mediaId").value(mediaId))
                .andExpect(jsonPath("$.title").value("Arrival (Updated)"));

        mockMvc.perform(delete("/media/{mediaId}", mediaId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/media/{mediaId}", mediaId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Media with id '" + mediaId + "' was not found"));
    }

    @Test
    void listMedia_WithPaginationAndFilter_ReturnsPagedDtoResponse() throws Exception {
        mediaRepository.saveAndFlush(new Media("media-1", "Lord of the Rings"));
        mediaRepository.saveAndFlush(new Media("media-2", "Ring"));
        mediaRepository.saveAndFlush(new Media("media-3", "Arrival"));

        mockMvc.perform(get("/media")
                        .param("title", "ring")
                        .param("page", "0")
                        .param("size", "2")
                        .param("sort", "title")
                        .param("direction", "asc"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[0].mediaId").value("media-1"))
                .andExpect(jsonPath("$.items[0].title").value("Lord of the Rings"))
                .andExpect(jsonPath("$.items[1].mediaId").value("media-2"))
                .andExpect(jsonPath("$.items[1].title").value("Ring"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void listMedia_WhenSortFieldUnsupported_ReturnsValidationErrorContract() throws Exception {
        mockMvc.perform(get("/media")
                        .param("sort", "id"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.sort").value("sort must be one of [mediaId, title]"));
    }

    @Test
    void openApiDocs_AreExposedWithMediaPaths() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.info.title").value("Libre Media Manager API"))
                .andExpect(jsonPath("$['paths']['/media']['get']['summary']").value("List media"))
                .andExpect(jsonPath("$['paths']['/media']['post']['summary']").value("Create media"))
                .andExpect(jsonPath("$['paths']['/media/{mediaId}']['get']['summary']").value("Get media"))
                .andExpect(jsonPath("$['paths']['/media/{mediaId}']['put']['summary']").value("Update media"))
                .andExpect(jsonPath("$['paths']['/media/{mediaId}']['delete']['summary']").value("Delete media"));
    }

    @Test
    void healthEndpoint_IsExposed() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/vnd.spring-boot.actuator.v3+json"))
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void requestIdHeader_IsEchoedBackWhenProvided() throws Exception {
        mockMvc.perform(get("/media")
                        .header(REQUEST_ID_HEADER, "request-123"))
                .andExpect(status().isOk())
                .andExpect(header().string(REQUEST_ID_HEADER, "request-123"));
    }

    @Test
    void requestIdHeader_IsGeneratedWhenAbsent() throws Exception {
        mockMvc.perform(get("/media"))
                .andExpect(status().isOk())
                .andExpect(header().string(REQUEST_ID_HEADER, matchesPattern(
                        "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
                )));
    }
}
