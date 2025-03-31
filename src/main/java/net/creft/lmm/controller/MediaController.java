package net.creft.lmm.controller;

import net.creft.lmm.response.MediaResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MediaController {

        @GetMapping("/media/{mediaId}")
        public MediaResponse getMedia(@PathVariable String mediaId) {
            // For now, we simply return a hard-coded title "Test" along with the provided mediaId.
            return new MediaResponse(mediaId, "Test");
        }


}
