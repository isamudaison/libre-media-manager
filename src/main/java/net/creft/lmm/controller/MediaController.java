package net.creft.lmm.controller;

import net.creft.lmm.model.Media;
import net.creft.lmm.repository.MediaRepository;
import net.creft.lmm.response.MediaResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MediaController {

    private final MediaRepository mediaRepository;

    public MediaController(MediaRepository mediaRepository) {
        this.mediaRepository = mediaRepository;
    }


    @GetMapping("/media/{mediaId}")
        public MediaResponse getMedia(@PathVariable String mediaId) {

        // Try to find the media by its mediaId in the database
        Media media = mediaRepository.findByMediaId(mediaId);
        if (media == null) {
            // For simplicity, if not found, create a new entry (or handle accordingly)
            media = new Media(mediaId, "Test");
            mediaRepository.save(media);
        }
        return new MediaResponse(media);
        }


}
