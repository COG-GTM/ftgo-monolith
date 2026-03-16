package net.chrisrichardson.ftgo.openapi.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Pagination metadata included in paged API responses.
 *
 * <p>Follows offset-based pagination conventions:
 * <pre>
 * {
 *   "page": 0,
 *   "size": 20,
 *   "totalElements": 142,
 *   "totalPages": 8
 * }
 * </pre>
 */
@Schema(description = "Pagination metadata")
public class PaginationMeta {

    @Schema(description = "Current page number (zero-based)", example = "0")
    private int page;

    @Schema(description = "Number of items per page", example = "20")
    private int size;

    @Schema(description = "Total number of items across all pages", example = "142")
    private long totalElements;

    @Schema(description = "Total number of pages", example = "8")
    private int totalPages;

    public PaginationMeta() {
    }

    public PaginationMeta(int page, int size, long totalElements, int totalPages) {
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }

    /**
     * Convenience factory that computes {@code totalPages} from
     * {@code totalElements} and {@code size}.
     */
    public static PaginationMeta of(int page, int size, long totalElements) {
        int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;
        return new PaginationMeta(page, size, totalElements, totalPages);
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
}
