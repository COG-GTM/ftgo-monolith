package com.ftgo.common.openapi.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * Standard API response envelope for all FTGO microservice endpoints.
 *
 * <p>All successful responses should be wrapped in this envelope to provide
 * a consistent structure across the platform. Example:
 *
 * <pre>
 * {
 *   "status": "success",
 *   "data": { ... },
 *   "timestamp": "2024-01-15T10:30:00Z",
 *   "path": "/api/v1/orders/123"
 * }
 * </pre>
 *
 * @param <T> the type of the response payload
 * @see ApiErrorResponse
 * @see PagedResponse
 */
@Schema(description = "Standard API response envelope")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    @Schema(description = "Response status indicator", example = "success", requiredMode = Schema.RequiredMode.REQUIRED)
    private String status;

    @Schema(description = "Response payload")
    private T data;

    @Schema(description = "ISO 8601 timestamp of the response", example = "2024-01-15T10:30:00Z")
    private Instant timestamp;

    @Schema(description = "Request path that generated this response", example = "/api/v1/orders/123")
    private String path;

    public ApiResponse() {
        this.timestamp = Instant.now();
    }

    private ApiResponse(String status, T data, String path) {
        this.status = status;
        this.data = data;
        this.timestamp = Instant.now();
        this.path = path;
    }

    /**
     * Creates a successful response with the given data.
     *
     * @param data the response payload
     * @param <T>  the payload type
     * @return a new ApiResponse with status "success"
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("success", data, null);
    }

    /**
     * Creates a successful response with the given data and request path.
     *
     * @param data the response payload
     * @param path the request path
     * @param <T>  the payload type
     * @return a new ApiResponse with status "success"
     */
    public static <T> ApiResponse<T> success(T data, String path) {
        return new ApiResponse<>("success", data, path);
    }

    /**
     * Creates a successful response with no data (for 204 No Content).
     *
     * @param <T> the payload type
     * @return a new ApiResponse with status "success" and null data
     */
    public static <T> ApiResponse<T> noContent() {
        return new ApiResponse<>("success", null, null);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
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
}
