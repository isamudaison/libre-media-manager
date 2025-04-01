package net.creft.lmm.controller;

import net.creft.lmm.dto.CreateMediaRequest;
import net.creft.lmm.model.Media;
import net.creft.lmm.repository.MediaRepository;
import net.creft.lmm.response.MediaResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
public class MediaController {

    private final MediaRepository mediaRepository;

    public MediaController(MediaRepository mediaRepository) {
        this.mediaRepository = mediaRepository;
    }

    @GetMapping("/media/{mediaId}")
    public ResponseEntity<Media> getMedia(@PathVariable String mediaId) {

        // Try to find the media by its mediaId in the database
        Media media = mediaRepository.findByMediaId(mediaId);
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

    @DeleteMapping("/media/{mediaId}")
    public ResponseEntity<Void> deleteMedia(@PathVariable String mediaId) {
        Media media = mediaRepository.findByMediaId(mediaId);
        if (media == null) {
            // If the media isn't found, return a 404 Not Found
            return ResponseEntity.notFound().build();
        }
        mediaRepository.delete(media);
        // Return a 204 No Content to indicate successful deletion
        return ResponseEntity.noContent().build();
    }


}
