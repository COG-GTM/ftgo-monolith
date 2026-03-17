package net.chrisrichardson.ftgo.openapi.pagination;

/**
 * Standard pagination constants used across all FTGO services.
 */
public final class PaginationConstants {

    private PaginationConstants() {
        // Prevent instantiation
    }

    /** Default page number (zero-based). */
    public static final int DEFAULT_PAGE = 0;

    /** Default page size. */
    public static final int DEFAULT_SIZE = 20;

    /** Maximum allowed page size to prevent excessive data retrieval. */
    public static final int MAX_SIZE = 100;

    /** Default sort direction. */
    public static final String DEFAULT_SORT_DIRECTION = "asc";

    /** Parameter name for page number. */
    public static final String PAGE_PARAM = "page";

    /** Parameter name for page size. */
    public static final String SIZE_PARAM = "size";

    /** Parameter name for sort field. */
    public static final String SORT_PARAM = "sort";
}
