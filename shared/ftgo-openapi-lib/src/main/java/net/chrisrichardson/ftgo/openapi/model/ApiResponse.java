package net.chrisrichardson.ftgo.openapi.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * Standard API response envelope for all FTGO REST endpoints.
 *
 * <p>All successful API responses should be wrapped in this envelope to provide
 * a consistent structure across all microservices. The envelope includes:</p>
 * <ul>
 *     <li>{@code status} — HTTP status description (e.g., "success", "error")</li>
 *     <li>{@code data} — The response payload (nullable on errors)</li>
 *     <li>{@code message} — Optional human-readable message</li>
 *     <li>{@code timestamp} — ISO 8601 timestamp of the response</li>
 * </ul>
 *
 * <h3>Usage Example</h3>
 * <pre>{@code
 * @GetMapping("/api/v1/orders/{id}")
 * public ResponseEntity<ApiResponse<OrderDTO>> getOrder(@PathVariable Long id) {
 *     OrderDTO order = orderService.findById(id);
 *     return ResponseEntity.ok(ApiResponse.success(order));
 * }
 * }</pre>
 *
 * @param <T> the type of the response payload
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard API response envelope")
public class ApiResponse<T> {

    @Schema(description = "Response status", example = "success")
    private final String status;

    @Schema(description = "Response payload")
    private final T data;

    @Schema(description = "Human-readable message", example = "Operation completed successfully")
    private final String message;

    @Schema(description = "ISO 8601 timestamp of the response", example = "2024-01-15T10:30:00Z")
    private final Instant timestamp;

    private ApiResponse(String status, T data, String message) {
        this.status = status;
        this.data = data;
        this.message = message;
        this.timestamp = Instant.now();
    }

    /**
     * Creates a successful response with data.
     *
     * @param data the response payload
     * @param <T>  the type of the payload
     * @return a new {@link ApiResponse} with status "success"
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("success", data, null);
    }

    /**
     * Creates a successful response with data and a message.
     *
     * @param data    the response payload
     * @param message a human-readable message
     * @param <T>     the type of the payload
     * @return a new {@link ApiResponse} with status "success"
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>("success", data, message);
    }

    /**
     * Creates an error response with a message.
     *
     * @param message the error message
     * @param <T>     the type of the payload (will be null)
     * @return a new {@link ApiResponse} with status "error"
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>("error", null, message);
    }

    public String getStatus() {
        return status;
    }

    public T getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
