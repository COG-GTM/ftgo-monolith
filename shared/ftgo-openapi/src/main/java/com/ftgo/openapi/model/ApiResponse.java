package com.ftgo.openapi.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

/**
 * Standard API response envelope for all FTGO REST endpoints.
 *
 * <p>All successful API responses should be wrapped in this envelope to provide
 * a consistent response format across all microservices.
 *
 * <h3>Success Response Example</h3>
 * <pre>
 * {
 *   "status": "success",
 *   "data": { ... },
 *   "timestamp": "2024-01-15T10:30:00Z"
 * }
 * </pre>
 *
 * <h3>Error Response Example</h3>
 * <pre>
 * {
 *   "status": "error",
 *   "error": {
 *     "code": "ORDER_NOT_FOUND",
 *     "message": "Order with ID 123 not found"
 *   },
 *   "timestamp": "2024-01-15T10:30:00Z"
 * }
 * </pre>
 *
 * @param <T> the type of the response payload
 * @see ApiError
 * @see PagedResponse
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private String status;
    private T data;
    private ApiError error;
    private String timestamp;

    public ApiResponse() {
        this.timestamp = Instant.now().toString();
    }

    /**
     * Creates a successful response wrapping the given data.
     *
     * @param data the response payload
     * @param <T>  the type of the payload
     * @return a success response
     */
    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setStatus("success");
        response.setData(data);
        return response;
    }

    /**
     * Creates an error response with the given error code and message.
     *
     * @param code    machine-readable error code (e.g., ORDER_NOT_FOUND)
     * @param message human-readable error message
     * @param <T>     the type parameter (typically Void for error responses)
     * @return an error response
     */
    public static <T> ApiResponse<T> error(String code, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setStatus("error");
        response.setError(new ApiError(code, message));
        return response;
    }

    /**
     * Creates an error response with the given error code, message, and details.
     *
     * @param code    machine-readable error code
     * @param message human-readable error message
     * @param details additional details about the error
     * @param <T>     the type parameter
     * @return an error response with details
     */
    public static <T> ApiResponse<T> error(String code, String message, String details) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setStatus("error");
        response.setError(new ApiError(code, message, details));
        return response;
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

    public ApiError getError() {
        return error;
    }

    public void setError(ApiError error) {
        this.error = error;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
