package net.creft.lmm.controller;

import net.creft.lmm.dto.CreateMediaRequest;
import net.creft.lmm.dto.UpdateMediaRequest;
import net.creft.lmm.model.Media;
import net.creft.lmm.repository.MediaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
public class MediaController {
    private static final Logger logger = LoggerFactory.getLogger(MediaController.class);

    private final MediaRepository mediaRepository;

    public MediaController(MediaRepository mediaRepository) {
        this.mediaRepository = mediaRepository;
    }

    @GetMapping("/media/{mediaId}")
    public ResponseEntity<Media> getMedia(@PathVariable String mediaId) {

        Media media = fetchMedia(mediaId);
        if (media == null) {
            // If the media isn't found, return a 404 Not Found
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.status(HttpStatus.OK).body(media);
    }

    // New POST endpoint for creating media
    @PostMapping("/media")
    public ResponseEntity<Media> createMedia(@RequestBody CreateMediaRequest request) {
        // Generate a unique media ID
        String generatedMediaId = UUID.randomUUID().toString();

        // Create and save the new Media entity
        Media media = new Media(generatedMediaId, request.getTitle());
        Media savedMedia = mediaRepository.save(media);

        // Return the created media with HTTP status 201 Created
        return ResponseEntity.status(HttpStatus.CREATED).body(savedMedia);
    }

    @PutMapping("/media/{mediaId}")
    public ResponseEntity<Media> updateMedia(@PathVariable String mediaId,
                                             @RequestBody UpdateMediaRequest updateRequest) {
        Media existingMedia = fetchMedia(mediaId);
        if (existingMedia == null) {
            return ResponseEntity.notFound().build();
        }
        // Update the title
        existingMedia.setTitle(updateRequest.getTitle());
        Media updatedMedia = mediaRepository.save(existingMedia);
        return ResponseEntity.ok(updatedMedia);
    }

    @DeleteMapping("/media/{mediaId}")
    public ResponseEntity<Void> deleteMedia(@PathVariable String mediaId) {
        Media media = fetchMedia(mediaId);

        if (media == null) {
            // If the media isn't found, return a 404 Not Found
            return ResponseEntity.notFound().build();
        }
        mediaRepository.delete(media);
        // Return a 204 No Content to indicate successful deletion
        return ResponseEntity.noContent().build();
    }

    private Media fetchMedia(String mediaId){
        logger.debug("Fetching media with ID: {}", mediaId);
        // Try to find the media by its mediaId in the database
        Media media = mediaRepository.findByMediaId(mediaId);
        if (media == null) {
            logger.info("No media with ID: {}", mediaId);
        }
        logger.info("Fetched media with ID: {}", mediaId);

        return media;
    }


}
