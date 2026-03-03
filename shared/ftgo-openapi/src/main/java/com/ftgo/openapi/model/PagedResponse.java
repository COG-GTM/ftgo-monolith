package com.ftgo.openapi.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Standard paginated response model for FTGO REST endpoints.
 *
 * <p>All list endpoints that return collections should use this model
 * to provide consistent pagination across all microservices.
 *
 * <h3>Example JSON</h3>
 * <pre>
 * {
 *   "status": "success",
 *   "data": [ ... ],
 *   "pagination": {
 *     "page": 0,
 *     "size": 20,
 *     "totalElements": 142,
 *     "totalPages": 8,
 *     "hasNext": true,
 *     "hasPrevious": false
 *   },
 *   "timestamp": "2024-01-15T10:30:00Z"
 * }
 * </pre>
 *
 * <h3>Query Parameters</h3>
 * <p>Standard pagination query parameters:
 * <ul>
 *   <li>{@code page} - Page number (0-based, default: 0)</li>
 *   <li>{@code size} - Page size (default: 20, max: 100)</li>
 *   <li>{@code sort} - Sort field and direction (e.g., {@code createdAt,desc})</li>
 * </ul>
 *
 * @param <T> the type of items in the page
 * @see ApiResponse
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PagedResponse<T> {

    private String status;
    private List<T> data;
    private PaginationMeta pagination;
    private String timestamp;

    public PagedResponse() {
    }

    /**
     * Creates a paginated success response.
     *
     * @param data          the list of items for the current page
     * @param page          current page number (0-based)
     * @param size          page size
     * @param totalElements total number of elements across all pages
     * @param <T>           the type of items
     * @return a paginated response
     */
    public static <T> PagedResponse<T> of(List<T> data, int page, int size, long totalElements) {
        PagedResponse<T> response = new PagedResponse<>();
        response.setStatus("success");
        response.setData(data);
        response.setTimestamp(java.time.Instant.now().toString());

        long totalPages = (totalElements + size - 1) / size;
        PaginationMeta pagination = new PaginationMeta();
        pagination.setPage(page);
        pagination.setSize(size);
        pagination.setTotalElements(totalElements);
        pagination.setTotalPages(totalPages);
        pagination.setHasNext(page < totalPages - 1);
        pagination.setHasPrevious(page > 0);
        response.setPagination(pagination);

        return response;
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

    public PaginationMeta getPagination() {
        return pagination;
    }

    public void setPagination(PaginationMeta pagination) {
        this.pagination = pagination;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Pagination metadata included in paginated responses.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PaginationMeta {

        private int page;
        private int size;
        private long totalElements;
        private long totalPages;
        private boolean hasNext;
        private boolean hasPrevious;

        public int getPage() {
            return page;
        }

        public void setPage(int page) {
            this.page = page;
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

        public long getTotalPages() {
            return totalPages;
        }

        public void setTotalPages(long totalPages) {
            this.totalPages = totalPages;
        }

        public boolean isHasNext() {
            return hasNext;
        }

        public void setHasNext(boolean hasNext) {
            this.hasNext = hasNext;
        }

        public boolean isHasPrevious() {
            return hasPrevious;
        }

        public void setHasPrevious(boolean hasPrevious) {
            this.hasPrevious = hasPrevious;
        }
    }
}
