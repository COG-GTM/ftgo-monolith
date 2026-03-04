package net.chrisrichardson.ftgo.openapi.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

/**
 * Standard paginated response envelope for FTGO REST endpoints.
 *
 * <p>All paginated API responses should use this envelope to ensure a consistent
 * pagination format across all microservices. The pagination metadata includes:</p>
 * <ul>
 *     <li>{@code page} — Current page number (0-based)</li>
 *     <li>{@code size} — Number of items per page</li>
 *     <li>{@code totalElements} — Total number of items across all pages</li>
 *     <li>{@code totalPages} — Total number of pages</li>
 *     <li>{@code sort} — Sort criteria (e.g., "createdAt,desc")</li>
 * </ul>
 *
 * <h3>Usage Example</h3>
 * <pre>{@code
 * @GetMapping("/api/v1/orders")
 * public ResponseEntity<PagedResponse<OrderDTO>> listOrders(
 *         @RequestParam(defaultValue = "0") int page,
 *         @RequestParam(defaultValue = "20") int size,
 *         @RequestParam(defaultValue = "createdAt,desc") String sort) {
 *     Page<OrderDTO> result = orderService.findAll(page, size, sort);
 *     return ResponseEntity.ok(PagedResponse.of(result.getContent(),
 *             page, size, result.getTotalElements(), sort));
 * }
 * }</pre>
 *
 * @param <T> the type of items in the page
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard paginated API response envelope")
public class PagedResponse<T> {

    @Schema(description = "Response status", example = "success")
    private final String status;

    @Schema(description = "List of items in the current page")
    private final List<T> data;

    @Schema(description = "Current page number (0-based)", example = "0")
    private final int page;

    @Schema(description = "Number of items per page", example = "20")
    private final int size;

    @Schema(description = "Total number of items across all pages", example = "150")
    private final long totalElements;

    @Schema(description = "Total number of pages", example = "8")
    private final int totalPages;

    @Schema(description = "Sort criteria", example = "createdAt,desc")
    private final String sort;

    @Schema(description = "ISO 8601 timestamp of the response", example = "2024-01-15T10:30:00Z")
    private final Instant timestamp;

    private PagedResponse(List<T> data, int page, int size, long totalElements, String sort) {
        this.status = "success";
        this.data = data;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;
        this.sort = sort;
        this.timestamp = Instant.now();
    }

    /**
     * Creates a paginated response.
     *
     * @param data          the list of items in the current page
     * @param page          the current page number (0-based)
     * @param size          the number of items per page
     * @param totalElements the total number of items across all pages
     * @param sort          the sort criteria (e.g., "createdAt,desc")
     * @param <T>           the type of items
     * @return a new {@link PagedResponse}
     */
    public static <T> PagedResponse<T> of(List<T> data, int page, int size, long totalElements, String sort) {
        return new PagedResponse<>(data, page, size, totalElements, sort);
    }

    /**
     * Creates a paginated response without sort information.
     *
     * @param data          the list of items in the current page
     * @param page          the current page number (0-based)
     * @param size          the number of items per page
     * @param totalElements the total number of items across all pages
     * @param <T>           the type of items
     * @return a new {@link PagedResponse}
     */
    public static <T> PagedResponse<T> of(List<T> data, int page, int size, long totalElements) {
        return new PagedResponse<>(data, page, size, totalElements, null);
    }

    public String getStatus() {
        return status;
    }

    public List<T> getData() {
        return data;
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

    public String getSort() {
        return sort;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
