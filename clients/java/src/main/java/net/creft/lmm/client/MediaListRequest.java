package net.creft.lmm.client;

import java.util.LinkedHashMap;
import java.util.Map;

public final class MediaListRequest {
    private final String title;
    private final String parentId;
    private final MediaType mediaType;
    private final MediaStatus status;
    private final String language;
    private final String releasedBefore;
    private final String releasedAfter;
    private final Integer page;
    private final Integer size;
    private final String sort;
    private final String direction;

    private MediaListRequest(Builder builder) {
        this.title = builder.title;
        this.parentId = builder.parentId;
        this.mediaType = builder.mediaType;
        this.status = builder.status;
        this.language = builder.language;
        this.releasedBefore = builder.releasedBefore;
        this.releasedAfter = builder.releasedAfter;
        this.page = builder.page;
        this.size = builder.size;
        this.sort = builder.sort;
        this.direction = builder.direction;
    }

    public static Builder builder() {
        return new Builder();
    }

    Map<String, String> toQueryParameters() {
        Map<String, String> query = new LinkedHashMap<>();
        putIfPresent(query, "title", title);
        putIfPresent(query, "parentId", parentId);
        putIfPresent(query, "mediaType", mediaType == null ? null : mediaType.name());
        putIfPresent(query, "status", status == null ? null : status.name());
        putIfPresent(query, "language", language);
        putIfPresent(query, "releasedBefore", releasedBefore);
        putIfPresent(query, "releasedAfter", releasedAfter);
        putIfPresent(query, "page", page == null ? null : String.valueOf(page));
        putIfPresent(query, "size", size == null ? null : String.valueOf(size));
        putIfPresent(query, "sort", sort);
        putIfPresent(query, "direction", direction);
        return query;
    }

    private static void putIfPresent(Map<String, String> query, String key, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        query.put(key, value);
    }

    public static final class Builder {
        private String title;
        private String parentId;
        private MediaType mediaType;
        private MediaStatus status;
        private String language;
        private String releasedBefore;
        private String releasedAfter;
        private Integer page;
        private Integer size;
        private String sort;
        private String direction;

        private Builder() {
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder parentId(String parentId) {
            this.parentId = parentId;
            return this;
        }

        public Builder mediaType(MediaType mediaType) {
            this.mediaType = mediaType;
            return this;
        }

        public Builder status(MediaStatus status) {
            this.status = status;
            return this;
        }

        public Builder language(String language) {
            this.language = language;
            return this;
        }

        public Builder releasedBefore(String releasedBefore) {
            this.releasedBefore = releasedBefore;
            return this;
        }

        public Builder releasedAfter(String releasedAfter) {
            this.releasedAfter = releasedAfter;
            return this;
        }

        public Builder page(Integer page) {
            this.page = page;
            return this;
        }

        public Builder size(Integer size) {
            this.size = size;
            return this;
        }

        public Builder sort(String sort) {
            this.sort = sort;
            return this;
        }

        public Builder direction(String direction) {
            this.direction = direction;
            return this;
        }

        public MediaListRequest build() {
            return new MediaListRequest(this);
        }
    }
}
