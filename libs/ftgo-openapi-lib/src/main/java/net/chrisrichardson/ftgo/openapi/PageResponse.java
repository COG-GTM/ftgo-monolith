package net.chrisrichardson.ftgo.openapi;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Standard paginated response wrapper for all FTGO microservices.
 *
 * @param <T> the type of items in the page
 */
@Schema(description = "Paginated response wrapper")
public class PageResponse<T> {

    @Schema(description = "Items in the current page")
    private List<T> content;

    @Schema(description = "Current page number (0-based)", example = "0")
    private int page;

    @Schema(description = "Page size", example = "20")
    private int size;

    @Schema(description = "Total number of items", example = "150")
    private long totalElements;

    @Schema(description = "Total number of pages", example = "8")
    private int totalPages;

    @Schema(description = "Whether this is the first page")
    private boolean first;

    @Schema(description = "Whether this is the last page")
    private boolean last;

    public PageResponse() {}

    public PageResponse(List<T> content, int page, int size, long totalElements) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;
        this.first = page == 0;
        this.last = page >= totalPages - 1;
    }

    public List<T> getContent() { return content; }
    public void setContent(List<T> content) { this.content = content; }
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
    public long getTotalElements() { return totalElements; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }
    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
    public boolean isFirst() { return first; }
    public void setFirst(boolean first) { this.first = first; }
    public boolean isLast() { return last; }
    public void setLast(boolean last) { this.last = last; }
}
