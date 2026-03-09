package net.creft.lmm.controller;

import jakarta.validation.Valid;
import net.creft.lmm.dto.CreateMediaRequest;
import net.creft.lmm.dto.UpdateMediaRequest;
import net.creft.lmm.model.Media;
import net.creft.lmm.service.MediaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class MediaController {
    private final MediaService mediaService;

    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @GetMapping("/media/{mediaId}")
    public ResponseEntity<Media> getMedia(@PathVariable String mediaId) {
        return ResponseEntity.status(HttpStatus.OK).body(mediaService.getMedia(mediaId));
    }

    @PostMapping("/media")
    public ResponseEntity<Media> createMedia(@Valid @RequestBody CreateMediaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mediaService.createMedia(request.getTitle()));
    }

    @PutMapping("/media/{mediaId}")
    public ResponseEntity<Media> updateMedia(@PathVariable String mediaId,
                                             @Valid @RequestBody UpdateMediaRequest updateRequest) {
        return ResponseEntity.ok(mediaService.updateMedia(mediaId, updateRequest.getTitle()));
    }

    @DeleteMapping("/media/{mediaId}")
    public ResponseEntity<Void> deleteMedia(@PathVariable String mediaId) {
        mediaService.deleteMedia(mediaId);
        return ResponseEntity.noContent().build();
    }
}
