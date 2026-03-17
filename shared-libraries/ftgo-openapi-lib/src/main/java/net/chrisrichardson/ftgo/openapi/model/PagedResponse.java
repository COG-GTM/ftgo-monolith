package net.chrisrichardson.ftgo.openapi.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

/**
 * Standardized paginated response format for list endpoints.
 *
 * <p>Example response:
 * <pre>
 * {
 *   "status": "success",
 *   "data": [ ... ],
 *   "page": 0,
 *   "size": 20,
 *   "totalElements": 150,
 *   "totalPages": 8,
 *   "sort": "createdAt,desc",
 *   "timestamp": "2024-01-15T10:30:00Z"
 * }
 * </pre>
 *
 * @param <T> the type of items in the paginated list
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Paginated API response")
public class PagedResponse<T> {

    @Schema(description = "Response status", example = "success")
    private String status;

    @Schema(description = "List of items in the current page")
    private List<T> data;

    @Schema(description = "Current page number (zero-based)", example = "0")
    private int page;

    @Schema(description = "Number of items per page", example = "20")
    private int size;

    @Schema(description = "Total number of items across all pages", example = "150")
    private long totalElements;

    @Schema(description = "Total number of pages", example = "8")
    private int totalPages;

    @Schema(description = "Sort criteria", example = "createdAt,desc")
    private String sort;

    @Schema(description = "ISO 8601 timestamp of the response", example = "2024-01-15T10:30:00Z")
    private Instant timestamp;

    public PagedResponse() {
        this.timestamp = Instant.now();
        this.status = "success";
    }

    public PagedResponse(List<T> data, int page, int size, long totalElements, String sort) {
        this.status = "success";
        this.data = data;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = (int) Math.ceil((double) totalElements / size);
        this.sort = sort;
        this.timestamp = Instant.now();
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

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
