package net.creft.lmm.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MediaPage(
        List<Media> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public MediaPage {
        items = items == null ? List.of() : List.copyOf(items);
    }
}
