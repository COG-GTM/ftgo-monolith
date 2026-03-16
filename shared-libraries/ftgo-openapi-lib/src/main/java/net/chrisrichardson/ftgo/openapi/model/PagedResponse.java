package net.chrisrichardson.ftgo.openapi.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Collections;
import java.util.List;

/**
 * Standard paginated response for collection endpoints.
 *
 * <p>Wraps a page of items together with pagination metadata:
 * <pre>
 * {
 *   "status": "success",
 *   "data": [ ... ],
 *   "pagination": { "page": 0, "size": 20, "totalElements": 142, "totalPages": 8 }
 * }
 * </pre>
 *
 * @param <T> the type of items in the page
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard paginated response")
public class PagedResponse<T> {

    @Schema(description = "Response status", example = "success")
    private String status;

    @Schema(description = "List of items on the current page")
    private List<T> data;

    @Schema(description = "Pagination metadata")
    private PaginationMeta pagination;

    public PagedResponse() {
    }

    private PagedResponse(String status, List<T> data, PaginationMeta pagination) {
        this.status = status;
        this.data = data;
        this.pagination = pagination;
    }

    /**
     * Creates a paged response from a list of items and pagination info.
     *
     * @param items         items on the current page
     * @param page          current page number (zero-based)
     * @param size          page size
     * @param totalElements total number of items across all pages
     * @param <T>           item type
     * @return a new paged response
     */
    public static <T> PagedResponse<T> of(List<T> items, int page, int size, long totalElements) {
        PaginationMeta meta = PaginationMeta.of(page, size, totalElements);
        return new PagedResponse<>("success", items, meta);
    }

    /**
     * Creates an empty paged response.
     *
     * @param page current page number (zero-based)
     * @param size page size
     * @param <T>  item type
     * @return a new empty paged response
     */
    public static <T> PagedResponse<T> empty(int page, int size) {
        PaginationMeta meta = PaginationMeta.of(page, size, 0);
        return new PagedResponse<>("success", Collections.<T>emptyList(), meta);
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
}
