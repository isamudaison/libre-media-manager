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
import net.creft.lmm.dto.MediaFileRequest;
import net.creft.lmm.dto.UpdateMediaRequest;
import net.creft.lmm.exception.ApiErrorResponse;
import net.creft.lmm.exception.InvalidRequestParameterException;
import net.creft.lmm.model.MediaStatus;
import net.creft.lmm.model.MediaType;
import net.creft.lmm.response.MediaPageResponse;
import net.creft.lmm.response.MediaResponse;
import net.creft.lmm.service.MediaDraft;
import net.creft.lmm.service.MediaFileDraft;
import net.creft.lmm.service.MediaSearchCriteria;
import net.creft.lmm.service.MediaService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@RestController
@Tag(name = "Media", description = "Operations for managing media records")
public class MediaController {
    private static final String MULTIPLE_PRIMARY_FILES_MESSAGE =
            "mediaFiles can contain at most one primaryFile=true entry";
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;
    private static final String DEFAULT_SORT_FIELD = "title";
    private static final String DEFAULT_SORT_DIRECTION = "asc";
    private static final Set<String> ALLOWED_SORT_FIELDS =
            Set.of("mediaId", "parentId", "title", "mediaType", "status", "releaseDate", "createdAt", "updatedAt");

    private final MediaService mediaService;

    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @GetMapping("/media")
    @Operation(
            summary = "List media",
            description = "Returns paginated media results with optional filtering by title, parentId, mediaType, status, language, and release-date bounds."
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
            @Parameter(description = "Optional parent media identifier filter", example = "c1c32f42-8919-4d6c-a0d8-9b4d42d2adbe")
            @RequestParam(required = false) String parentId,
            @Parameter(description = "Optional media type filter", example = "MOVIE")
            @RequestParam(required = false) MediaType mediaType,
            @Parameter(description = "Optional lifecycle status filter", example = "ACTIVE")
            @RequestParam(required = false) MediaStatus status,
            @Parameter(description = "Optional case-insensitive language filter", example = "en")
            @RequestParam(required = false) String language,
            @Parameter(description = "Optional inclusive upper release-date bound", example = "2016-12-31")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate releasedBefore,
            @Parameter(description = "Optional inclusive lower release-date bound", example = "2016-01-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate releasedAfter,
            @Parameter(description = "Zero-based page number", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size between 1 and 100", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field. Allowed values: mediaId, parentId, title, mediaType, status, releaseDate, createdAt, updatedAt", example = "title")
            @RequestParam(defaultValue = DEFAULT_SORT_FIELD) String sort,
            @Parameter(description = "Sort direction. Allowed values: asc, desc", example = "asc")
            @RequestParam(defaultValue = DEFAULT_SORT_DIRECTION) String direction
    ) {
        Pageable pageable = buildPageRequest(page, size, sort, direction);
        validateReleaseDateRange(releasedAfter, releasedBefore);
        return ResponseEntity.ok(MediaPageResponse.from(mediaService.listMedia(
                new MediaSearchCriteria(title, parentId, mediaType, status, language, releasedBefore, releasedAfter),
                pageable
        )));
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
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(MediaResponse.from(mediaService.createMedia(toMediaDraft(request))));
    }

    @PutMapping("/media/{mediaId}")
    @Operation(summary = "Update media", description = "Updates an existing media item and replaces its associated mediaFiles list.")
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
        return ResponseEntity.ok(MediaResponse.from(mediaService.updateMedia(mediaId, toMediaDraft(updateRequest))));
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
            throw new InvalidRequestParameterException(
                    "sort",
                    "sort must be one of [mediaId, parentId, title, mediaType, status, releaseDate, createdAt, updatedAt]"
            );
        }

        Sort.Direction sortDirection = Sort.Direction.fromOptionalString(direction)
                .orElseThrow(() -> new InvalidRequestParameterException("direction", "direction must be 'asc' or 'desc'"));

        return PageRequest.of(page, size, Sort.by(sortDirection, normalizedSort));
    }

    private List<MediaFileDraft> extractMediaFiles(List<MediaFileRequest> mediaFiles) {
        if (mediaFiles == null) {
            return List.of();
        }
        validatePrimaryFileSelection(mediaFiles);
        return mediaFiles.stream()
                .map(mediaFile -> new MediaFileDraft(
                        mediaFile.getLocation(),
                        mediaFile.getLabel(),
                        mediaFile.getMimeType(),
                        mediaFile.getSizeBytes(),
                        mediaFile.getDurationSeconds(),
                        mediaFile.isPrimaryFile()
                ))
                .toList();
    }

    private void validatePrimaryFileSelection(List<MediaFileRequest> mediaFiles) {
        long primaryFiles = mediaFiles.stream()
                .filter(MediaFileRequest::isPrimaryFile)
                .count();
        if (primaryFiles > 1) {
            throw new InvalidRequestParameterException("mediaFiles", MULTIPLE_PRIMARY_FILES_MESSAGE);
        }
    }

    private void validateReleaseDateRange(LocalDate releasedAfter, LocalDate releasedBefore) {
        if (releasedAfter != null && releasedBefore != null && releasedAfter.isAfter(releasedBefore)) {
            throw new InvalidRequestParameterException(
                    "releasedAfter",
                    "releasedAfter must be on or before releasedBefore"
            );
        }
    }

    private MediaDraft toMediaDraft(CreateMediaRequest request) {
        return new MediaDraft(
                request.getTitle(),
                request.getOriginalTitle(),
                request.getMediaType(),
                request.getStatus(),
                request.getSummary(),
                request.getReleaseDate(),
                request.getRuntimeMinutes(),
                request.getLanguage(),
                request.getParentId(),
                extractMediaFiles(request.getMediaFiles())
        );
    }

    private MediaDraft toMediaDraft(UpdateMediaRequest request) {
        return new MediaDraft(
                request.getTitle(),
                request.getOriginalTitle(),
                request.getMediaType(),
                request.getStatus(),
                request.getSummary(),
                request.getReleaseDate(),
                request.getRuntimeMinutes(),
                request.getLanguage(),
                request.getParentId(),
                extractMediaFiles(request.getMediaFiles())
        );
    }
}
