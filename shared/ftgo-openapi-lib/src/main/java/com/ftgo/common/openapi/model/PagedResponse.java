package com.ftgo.common.openapi.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

/**
 * Standard paginated response format for all FTGO microservice endpoints.
 *
 * <p>All paginated list endpoints should return this structure. Example:
 *
 * <pre>
 * {
 *   "status": "success",
 *   "data": [ ... ],
 *   "page": {
 *     "number": 0,
 *     "size": 20,
 *     "totalElements": 150,
 *     "totalPages": 8
 *   },
 *   "timestamp": "2024-01-15T10:30:00Z",
 *   "path": "/api/v1/orders?page=0&size=20"
 * }
 * </pre>
 *
 * @param <T> the type of items in the page
 * @see ApiResponse
 */
@Schema(description = "Standard paginated API response")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PagedResponse<T> {

    @Schema(description = "Response status indicator", example = "success", requiredMode = Schema.RequiredMode.REQUIRED)
    private String status;

    @Schema(description = "Page of items")
    private List<T> data;

    @Schema(description = "Pagination metadata", requiredMode = Schema.RequiredMode.REQUIRED)
    private PageMetadata page;

    @Schema(description = "ISO 8601 timestamp of the response", example = "2024-01-15T10:30:00Z")
    private Instant timestamp;

    @Schema(description = "Request path that generated this response", example = "/api/v1/orders?page=0&size=20")
    private String path;

    public PagedResponse() {
        this.status = "success";
        this.timestamp = Instant.now();
    }

    private PagedResponse(List<T> data, int number, int size, long totalElements, int totalPages, String path) {
        this.status = "success";
        this.data = data;
        this.page = new PageMetadata(number, size, totalElements, totalPages);
        this.timestamp = Instant.now();
        this.path = path;
    }

    /**
     * Creates a paginated response.
     *
     * @param data          the list of items for this page
     * @param number        the zero-based page number
     * @param size          the page size
     * @param totalElements the total number of elements across all pages
     * @param totalPages    the total number of pages
     * @param <T>           the item type
     * @return a new PagedResponse
     */
    public static <T> PagedResponse<T> of(List<T> data, int number, int size, long totalElements, int totalPages) {
        return new PagedResponse<>(data, number, size, totalElements, totalPages, null);
    }

    /**
     * Creates a paginated response with a request path.
     *
     * @param data          the list of items for this page
     * @param number        the zero-based page number
     * @param size          the page size
     * @param totalElements the total number of elements across all pages
     * @param totalPages    the total number of pages
     * @param path          the request path
     * @param <T>           the item type
     * @return a new PagedResponse
     */
    public static <T> PagedResponse<T> of(List<T> data, int number, int size, long totalElements, int totalPages, String path) {
        return new PagedResponse<>(data, number, size, totalElements, totalPages, path);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public PageMetadata getPage() {
        return page;
    }

    public void setPage(PageMetadata page) {
        this.page = page;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Pagination metadata describing the current page and total data set.
     */
    @Schema(description = "Pagination metadata")
    public static class PageMetadata {

        @Schema(description = "Zero-based page number", example = "0", requiredMode = Schema.RequiredMode.REQUIRED)
        private int number;

        @Schema(description = "Number of items per page", example = "20", requiredMode = Schema.RequiredMode.REQUIRED)
        private int size;

        @Schema(description = "Total number of elements across all pages", example = "150", requiredMode = Schema.RequiredMode.REQUIRED)
        private long totalElements;

        @Schema(description = "Total number of pages", example = "8", requiredMode = Schema.RequiredMode.REQUIRED)
        private int totalPages;

        public PageMetadata() {
        }

        public PageMetadata(int number, int size, long totalElements, int totalPages) {
            this.number = number;
            this.size = size;
            this.totalElements = totalElements;
            this.totalPages = totalPages;
        }

        public int getNumber() {
            return number;
        }

        public void setNumber(int number) {
            this.number = number;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }

        public long getTotalElements() {
            return totalElements;
        }

        public void setTotalElements(long totalElements) {
            this.totalElements = totalElements;
        }

        public int getTotalPages() {
            return totalPages;
        }

        public void setTotalPages(int totalPages) {
            this.totalPages = totalPages;
        }
    }
}
