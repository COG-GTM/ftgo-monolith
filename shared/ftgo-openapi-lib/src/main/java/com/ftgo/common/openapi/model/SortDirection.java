package com.ftgo.common.openapi.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Sort direction for paginated queries.
 *
 * <p>Used in conjunction with sort field parameters to specify ordering.
 * Example query: {@code GET /api/v1/orders?sort=createdAt&direction=DESC}
 */
@Schema(description = "Sort direction for paginated queries")
public enum SortDirection {

    @Schema(description = "Ascending order (A-Z, 0-9, oldest first)")
    ASC,

    @Schema(description = "Descending order (Z-A, 9-0, newest first)")
    DESC
}
