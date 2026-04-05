package net.creft.lmm.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.creft.lmm.model.Media;
import net.creft.lmm.model.MediaFile;
import net.creft.lmm.model.MediaStatus;
import net.creft.lmm.model.MediaType;
import net.creft.lmm.repository.MediaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Arrival",
                                  "originalTitle": "Story of Your Life",
                                  "mediaType": "MOVIE",
                                  "summary": "A linguist is recruited to communicate with extraterrestrial visitors.",
                                  "releaseDate": "2016-11-11",
                                  "runtimeMinutes": 116,
                                  "language": "en",
                                  "mediaFiles": [
                                    {
                                      "location": "/srv/media/arrival.mkv",
                                      "label": "Main Feature",
                                      "mimeType": "video/x-matroska",
                                      "sizeBytes": 7340032000,
                                      "durationSeconds": 6960,
                                      "primaryFile": true
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(org.springframework.http.MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.mediaId").isString())
                .andExpect(jsonPath("$.title").value("Arrival"))
                .andExpect(jsonPath("$.originalTitle").value("Story of Your Life"))
                .andExpect(jsonPath("$.mediaType").value("MOVIE"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.summary").value("A linguist is recruited to communicate with extraterrestrial visitors."))
                .andExpect(jsonPath("$.releaseDate").value("2016-11-11"))
                .andExpect(jsonPath("$.runtimeMinutes").value(116))
                .andExpect(jsonPath("$.language").value("en"))
                .andExpect(jsonPath("$.createdAt").isString())
                .andExpect(jsonPath("$.updatedAt").isString())
                .andExpect(jsonPath("$.mediaFiles[0].location").value("/srv/media/arrival.mkv"))
                .andExpect(jsonPath("$.mediaFiles[0].label").value("Main Feature"))
                .andExpect(jsonPath("$.mediaFiles[0].mimeType").value("video/x-matroska"))
                .andExpect(jsonPath("$.mediaFiles[0].sizeBytes").value(7340032000L))
                .andExpect(jsonPath("$.mediaFiles[0].durationSeconds").value(6960))
                .andExpect(jsonPath("$.mediaFiles[0].primaryFile").value(true))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode createdMedia = objectMapper.readTree(createResponseBody);
        String mediaId = createdMedia.path("mediaId").asText();
        Instant createdAt = Instant.parse(createdMedia.path("createdAt").asText());

        mockMvc.perform(get("/media/{mediaId}", mediaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mediaId").value(mediaId))
                .andExpect(jsonPath("$.title").value("Arrival"))
                .andExpect(jsonPath("$.originalTitle").value("Story of Your Life"))
                .andExpect(jsonPath("$.mediaType").value("MOVIE"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.summary").value("A linguist is recruited to communicate with extraterrestrial visitors."))
                .andExpect(jsonPath("$.releaseDate").value("2016-11-11"))
                .andExpect(jsonPath("$.runtimeMinutes").value(116))
                .andExpect(jsonPath("$.language").value("en"))
                .andExpect(jsonPath("$.mediaFiles[0].location").value("/srv/media/arrival.mkv"))
                .andExpect(jsonPath("$.mediaFiles[0].label").value("Main Feature"))
                .andExpect(jsonPath("$.mediaFiles[0].primaryFile").value(true));

        String updateResponseBody = mockMvc.perform(put("/media/{mediaId}", mediaId)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Arrival (Updated)",
                                  "originalTitle": "Story of Your Life",
                                  "mediaType": "MOVIE",
                                  "status": "ARCHIVED",
                                  "summary": "Updated summary",
                                  "releaseDate": "2017-01-01",
                                  "runtimeMinutes": 117,
                                  "language": "en-US",
                                  "mediaFiles": [
                                    {
                                      "location": "/srv/media/arrival-4k.mkv",
                                      "label": "4K Remux",
                                      "mimeType": "video/x-matroska",
                                      "sizeBytes": 18340032000,
                                      "durationSeconds": 6960,
                                      "primaryFile": true
                                    },
                                    {
                                      "location": "/srv/media/arrival-commentary.mkv",
                                      "label": "Commentary Track",
                                      "mimeType": "audio/flac",
                                      "sizeBytes": 834003200,
                                      "durationSeconds": 7020,
                                      "primaryFile": false
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mediaId").value(mediaId))
                .andExpect(jsonPath("$.title").value("Arrival (Updated)"))
                .andExpect(jsonPath("$.originalTitle").value("Story of Your Life"))
                .andExpect(jsonPath("$.mediaType").value("MOVIE"))
                .andExpect(jsonPath("$.status").value("ARCHIVED"))
                .andExpect(jsonPath("$.summary").value("Updated summary"))
                .andExpect(jsonPath("$.releaseDate").value("2017-01-01"))
                .andExpect(jsonPath("$.runtimeMinutes").value(117))
                .andExpect(jsonPath("$.language").value("en-US"))
                .andExpect(jsonPath("$.createdAt").isString())
                .andExpect(jsonPath("$.updatedAt").isString())
                .andExpect(jsonPath("$.mediaFiles[0].location").value("/srv/media/arrival-4k.mkv"))
                .andExpect(jsonPath("$.mediaFiles[0].label").value("4K Remux"))
                .andExpect(jsonPath("$.mediaFiles[0].primaryFile").value(true))
                .andExpect(jsonPath("$.mediaFiles[1].location").value("/srv/media/arrival-commentary.mkv"))
                .andExpect(jsonPath("$.mediaFiles[1].label").value("Commentary Track"))
                .andExpect(jsonPath("$.mediaFiles[1].mimeType").value("audio/flac"))
                .andExpect(jsonPath("$.mediaFiles[1].sizeBytes").value(834003200))
                .andExpect(jsonPath("$.mediaFiles[1].durationSeconds").value(7020))
                .andExpect(jsonPath("$.mediaFiles[1].primaryFile").value(false))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode updatedMedia = objectMapper.readTree(updateResponseBody);
        Instant updatedAt = Instant.parse(updatedMedia.path("updatedAt").asText());
        assertFalse(updatedAt.isBefore(createdAt));

        mockMvc.perform(delete("/media/{mediaId}", mediaId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/media/{mediaId}", mediaId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Media with id '" + mediaId + "' was not found"));
    }

    @Test
    void createUpdateQueryDeleteMedia_EndToEnd() throws Exception {
        String createResponseBody = mockMvc.perform(post("/media")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Solaris",
                                  "originalTitle": "Солярис",
                                  "mediaType": "MOVIE",
                                  "summary": "A psychologist is sent to a space station orbiting Solaris.",
                                  "releaseDate": "1972-03-20",
                                  "runtimeMinutes": 167,
                                  "language": "ru",
                                  "mediaFiles": [
                                    {
                                      "location": "/srv/media/solaris.mkv",
                                      "label": "Theatrical Cut",
                                      "mimeType": "video/x-matroska",
                                      "sizeBytes": 1234567890,
                                      "durationSeconds": 10020,
                                      "primaryFile": true
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(org.springframework.http.MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value("Solaris"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String mediaId = objectMapper.readTree(createResponseBody).path("mediaId").asText();

        mockMvc.perform(put("/media/{mediaId}", mediaId)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Solaris (Criterion)",
                                  "originalTitle": "Солярис",
                                  "mediaType": "MOVIE",
                                  "status": "ARCHIVED",
                                  "summary": "Criterion restoration of Tarkovsky's Solaris.",
                                  "releaseDate": "1972-03-20",
                                  "runtimeMinutes": 167,
                                  "language": "ru",
                                  "mediaFiles": [
                                    {
                                      "location": "/srv/media/solaris-criterion.mkv",
                                      "label": "Criterion Restoration",
                                      "mimeType": "video/x-matroska",
                                      "sizeBytes": 2234567890,
                                      "durationSeconds": 10020,
                                      "primaryFile": true
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mediaId").value(mediaId))
                .andExpect(jsonPath("$.title").value("Solaris (Criterion)"))
                .andExpect(jsonPath("$.status").value("ARCHIVED"))
                .andExpect(jsonPath("$.summary").value("Criterion restoration of Tarkovsky's Solaris."))
                .andExpect(jsonPath("$.mediaFiles[0].location").value("/srv/media/solaris-criterion.mkv"));

        mockMvc.perform(get("/media")
                        .param("title", "criterion")
                        .param("mediaType", "MOVIE")
                        .param("status", "ARCHIVED")
                        .param("language", "ru")
                        .param("releasedAfter", "1972-01-01")
                        .param("releasedBefore", "1972-12-31")
                        .param("sort", "releaseDate")
                        .param("direction", "desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(org.springframework.http.MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].mediaId").value(mediaId))
                .andExpect(jsonPath("$.items[0].title").value("Solaris (Criterion)"))
                .andExpect(jsonPath("$.items[0].originalTitle").value("Солярис"))
                .andExpect(jsonPath("$.items[0].mediaType").value("MOVIE"))
                .andExpect(jsonPath("$.items[0].status").value("ARCHIVED"))
                .andExpect(jsonPath("$.items[0].summary").value("Criterion restoration of Tarkovsky's Solaris."))
                .andExpect(jsonPath("$.items[0].releaseDate").value("1972-03-20"))
                .andExpect(jsonPath("$.items[0].runtimeMinutes").value(167))
                .andExpect(jsonPath("$.items[0].language").value("ru"))
                .andExpect(jsonPath("$.items[0].mediaFiles[0].location").value("/srv/media/solaris-criterion.mkv"))
                .andExpect(jsonPath("$.totalElements").value(1));

        mockMvc.perform(delete("/media/{mediaId}", mediaId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/media")
                        .param("title", "criterion")
                        .param("mediaType", "MOVIE")
                        .param("status", "ARCHIVED")
                        .param("language", "ru")
                        .param("releasedAfter", "1972-01-01")
                        .param("releasedBefore", "1972-12-31"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(org.springframework.http.MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.items.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void listMedia_WithRichFiltersAndSorting_ReturnsPagedDtoResponse() throws Exception {
        mediaRepository.saveAndFlush(buildSeedMedia(
                "media-1",
                "Ring Documentary",
                MediaType.MOVIE,
                MediaStatus.ACTIVE,
                "en",
                LocalDate.parse("2015-01-01"),
                List.of(new MediaFile("/srv/media/lotr.mkv", "Main Feature", "video/x-matroska", 9340032000L, 10800, true))
        ));
        mediaRepository.saveAndFlush(buildSeedMedia(
                "media-2",
                "Lord of the Rings",
                MediaType.MOVIE,
                MediaStatus.ACTIVE,
                "EN",
                LocalDate.parse("2016-01-01"),
                List.of(new MediaFile("/srv/media/lotr-extended.mkv", "Extended Edition", "video/x-matroska", 12340032000L, 12800, true))
        ));
        mediaRepository.saveAndFlush(buildSeedMedia(
                "media-3",
                "Ring",
                MediaType.BOOK,
                MediaStatus.ARCHIVED,
                "en",
                LocalDate.parse("2016-06-01"),
                List.of(new MediaFile("/srv/media/ring.mkv", "Main Feature", "video/mp4", 4340032000L, 6900, true))
        ));
        mediaRepository.saveAndFlush(buildSeedMedia(
                "media-4",
                "Arrival",
                MediaType.SERIES,
                MediaStatus.ACTIVE,
                "fr",
                LocalDate.parse("2017-01-01"),
                List.of(new MediaFile("/srv/media/arrival.mkv"))
        ));

        mockMvc.perform(get("/media")
                        .param("title", "ring")
                        .param("mediaType", "MOVIE")
                        .param("status", "ACTIVE")
                        .param("language", "en")
                        .param("releasedAfter", "2015-01-01")
                        .param("releasedBefore", "2016-12-31")
                        .param("page", "0")
                        .param("size", "5")
                        .param("sort", "releaseDate")
                        .param("direction", "desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(org.springframework.http.MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[0].mediaId").value("media-2"))
                .andExpect(jsonPath("$.items[0].title").value("Lord of the Rings"))
                .andExpect(jsonPath("$.items[0].mediaType").value("MOVIE"))
                .andExpect(jsonPath("$.items[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$.items[0].language").value("EN"))
                .andExpect(jsonPath("$.items[0].releaseDate").value("2016-01-01"))
                .andExpect(jsonPath("$.items[0].mediaFiles[0].location").value("/srv/media/lotr-extended.mkv"))
                .andExpect(jsonPath("$.items[0].mediaFiles[0].label").value("Extended Edition"))
                .andExpect(jsonPath("$.items[0].mediaFiles[0].primaryFile").value(true))
                .andExpect(jsonPath("$.items[1].mediaId").value("media-1"))
                .andExpect(jsonPath("$.items[1].title").value("Ring Documentary"))
                .andExpect(jsonPath("$.items[1].mediaType").value("MOVIE"))
                .andExpect(jsonPath("$.items[1].status").value("ACTIVE"))
                .andExpect(jsonPath("$.items[1].language").value("en"))
                .andExpect(jsonPath("$.items[1].releaseDate").value("2015-01-01"))
                .andExpect(jsonPath("$.items[1].mediaFiles[0].location").value("/srv/media/lotr.mkv"))
                .andExpect(jsonPath("$.items[1].mediaFiles[0].mimeType").value("video/x-matroska"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void listMedia_WhenReleaseDateRangeInvalid_ReturnsValidationErrorContract() throws Exception {
        mockMvc.perform(get("/media")
                        .param("releasedAfter", "2017-01-01")
                        .param("releasedBefore", "2016-01-01"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(org.springframework.http.MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.releasedAfter")
                        .value("releasedAfter must be on or before releasedBefore"));
    }

    @Test
    void createMedia_WhenMediaTypeMissing_ReturnsValidationError() throws Exception {
        mockMvc.perform(post("/media")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Arrival"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(org.springframework.http.MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.mediaType").value("mediaType is required"));
    }

    @Test
    void createMedia_WhenRuntimeMinutesNonPositive_ReturnsValidationError() throws Exception {
        mockMvc.perform(post("/media")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Arrival",
                                  "mediaType": "MOVIE",
                                  "runtimeMinutes": 0
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(org.springframework.http.MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.runtimeMinutes").value("runtimeMinutes must be greater than 0"));
    }

    @Test
    void createMedia_WhenMultiplePrimaryFilesRequested_ReturnsValidationError() throws Exception {
        mockMvc.perform(post("/media")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Arrival",
                                  "mediaType": "MOVIE",
                                  "mediaFiles": [
                                    {
                                      "location": "/srv/media/arrival-main.mkv",
                                      "primaryFile": true
                                    },
                                    {
                                      "location": "/srv/media/arrival-backup.mkv",
                                      "primaryFile": true
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(org.springframework.http.MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.mediaFiles")
                        .value("mediaFiles can contain at most one primaryFile=true entry"));
    }

    @Test
    void listMedia_WhenSortFieldUnsupported_ReturnsValidationErrorContract() throws Exception {
        mockMvc.perform(get("/media")
                        .param("sort", "id"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(org.springframework.http.MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.sort")
                        .value("sort must be one of [mediaId, title, mediaType, status, releaseDate, createdAt, updatedAt]"));
    }

    @Test
    void openApiDocs_AreExposedWithMediaPaths() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(org.springframework.http.MediaType.APPLICATION_JSON))
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

    private Media buildSeedMedia(
            String mediaId,
            String title,
            MediaType mediaType,
            MediaStatus status,
            String language,
            LocalDate releaseDate,
            List<MediaFile> mediaFiles
    ) {
        Media media = new Media(mediaId, title, mediaFiles);
        media.setOriginalTitle("Original " + title);
        media.setMediaType(mediaType);
        media.setStatus(status);
        media.setSummary("Summary for " + title);
        media.setReleaseDate(releaseDate);
        media.setRuntimeMinutes(116);
        media.setLanguage(language);
        return media;
    }
}
