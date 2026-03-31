package net.creft.lmm.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import net.creft.lmm.dto.CreateMediaRequest;
import net.creft.lmm.dto.UpdateMediaRequest;
import net.creft.lmm.exception.ApiErrorResponse;
import net.creft.lmm.exception.InvalidRequestParameterException;
import net.creft.lmm.response.MediaPageResponse;
import net.creft.lmm.response.MediaResponse;
import net.creft.lmm.service.MediaService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@Tag(name = "Media", description = "Operations for managing media records")
public class MediaController {
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;
    private static final String DEFAULT_SORT_FIELD = "title";
    private static final String DEFAULT_SORT_DIRECTION = "asc";
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("mediaId", "title");

    private final MediaService mediaService;

    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @GetMapping("/media")
    @Operation(
            summary = "List media",
            description = "Returns paginated media results with optional case-insensitive title filtering."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Media page returned"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid paging or sorting parameters",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<MediaPageResponse> listMedia(
            @Parameter(description = "Optional case-insensitive title filter", example = "arrival")
            @RequestParam(required = false) String title,
            @Parameter(description = "Zero-based page number", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size between 1 and 100", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field. Allowed values: mediaId, title", example = "title")
            @RequestParam(defaultValue = DEFAULT_SORT_FIELD) String sort,
            @Parameter(description = "Sort direction. Allowed values: asc, desc", example = "asc")
            @RequestParam(defaultValue = DEFAULT_SORT_DIRECTION) String direction
    ) {
        Pageable pageable = buildPageRequest(page, size, sort, direction);
        return ResponseEntity.ok(MediaPageResponse.from(mediaService.listMedia(title, pageable)));
    }

    @GetMapping("/media/{mediaId}")
    @Operation(summary = "Get media", description = "Returns a single media item by its public mediaId.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Media returned"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Media not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<MediaResponse> getMedia(@PathVariable String mediaId) {
        return ResponseEntity.status(HttpStatus.OK).body(MediaResponse.from(mediaService.getMedia(mediaId)));
    }

    @PostMapping("/media")
    @Operation(summary = "Create media", description = "Creates a new media record with a generated mediaId.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Media created"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation or request parsing failure",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Request conflicts with existing data",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<MediaResponse> createMedia(@Valid @RequestBody CreateMediaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(MediaResponse.from(mediaService.createMedia(request.getTitle())));
    }

    @PutMapping("/media/{mediaId}")
    @Operation(summary = "Update media", description = "Updates the title of an existing media item.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Media updated"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation or request parsing failure",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Media not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Request conflicts with existing data",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<MediaResponse> updateMedia(@PathVariable String mediaId,
                                                     @Valid @RequestBody UpdateMediaRequest updateRequest) {
        return ResponseEntity.ok(MediaResponse.from(mediaService.updateMedia(mediaId, updateRequest.getTitle())));
    }

    @DeleteMapping("/media/{mediaId}")
    @Operation(summary = "Delete media", description = "Deletes an existing media item by mediaId.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Media deleted"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Media not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<Void> deleteMedia(@PathVariable String mediaId) {
        mediaService.deleteMedia(mediaId);
        return ResponseEntity.noContent().build();
    }

    private Pageable buildPageRequest(int page, int size, String sort, String direction) {
        if (page < 0) {
            throw new InvalidRequestParameterException("page", "page must be greater than or equal to 0");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new InvalidRequestParameterException("size", "size must be between 1 and 100");
        }

        String normalizedSort = sort == null ? DEFAULT_SORT_FIELD : sort.trim();
        if (!ALLOWED_SORT_FIELDS.contains(normalizedSort)) {
            throw new InvalidRequestParameterException("sort", "sort must be one of [mediaId, title]");
        }

        Sort.Direction sortDirection = Sort.Direction.fromOptionalString(direction)
                .orElseThrow(() -> new InvalidRequestParameterException("direction", "direction must be 'asc' or 'desc'"));

        return PageRequest.of(page, size, Sort.by(sortDirection, normalizedSort));
    }
}
