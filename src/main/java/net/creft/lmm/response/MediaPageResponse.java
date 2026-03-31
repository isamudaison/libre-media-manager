package net.creft.lmm.response;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import net.creft.lmm.model.Media;
import org.springframework.data.domain.Page;

import java.util.List;

@Schema(description = "Paginated media response")
public class MediaPageResponse {
    @ArraySchema(schema = @Schema(implementation = MediaResponse.class))
    private final List<MediaResponse> items;

    @Schema(description = "Zero-based page number", example = "0")
    private final int page;

    @Schema(description = "Requested page size", example = "20")
    private final int size;

    @Schema(description = "Total number of matching records", example = "42")
    private final long totalElements;

    @Schema(description = "Total number of available pages", example = "3")
    private final int totalPages;

    public MediaPageResponse(List<MediaResponse> items, int page, int size, long totalElements, int totalPages) {
        this.items = List.copyOf(items);
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }

    public static MediaPageResponse from(Page<Media> mediaPage) {
        List<MediaResponse> items = mediaPage.getContent().stream()
                .map(MediaResponse::from)
                .toList();
        return new MediaPageResponse(
                items,
                mediaPage.getNumber(),
                mediaPage.getSize(),
                mediaPage.getTotalElements(),
                mediaPage.getTotalPages()
        );
    }

    public List<MediaResponse> getItems() {
        return items;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }
}
