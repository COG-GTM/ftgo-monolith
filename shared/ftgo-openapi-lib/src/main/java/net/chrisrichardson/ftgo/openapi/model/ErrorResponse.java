package net.chrisrichardson.ftgo.openapi.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

/**
 * Standard error response envelope for FTGO REST endpoints.
 *
 * <p>All error responses should use this envelope to provide a consistent error
 * format across all microservices. The error envelope includes:</p>
 * <ul>
 *     <li>{@code status} — Always "error"</li>
 *     <li>{@code code} — Application-specific error code</li>
 *     <li>{@code message} — Human-readable error message</li>
 *     <li>{@code details} — Optional list of field-level validation errors</li>
 *     <li>{@code path} — The request path that caused the error</li>
 *     <li>{@code timestamp} — ISO 8601 timestamp of the error</li>
 * </ul>
 *
 * <h3>Usage Example</h3>
 * <pre>{@code
 * @ExceptionHandler(OrderNotFoundException.class)
 * public ResponseEntity<ErrorResponse> handleNotFound(OrderNotFoundException ex,
 *                                                     HttpServletRequest request) {
 *     ErrorResponse error = ErrorResponse.of("ORDER_NOT_FOUND", ex.getMessage(),
 *                                            request.getRequestURI());
 *     return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
 * }
 * }</pre>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard error response envelope")
public class ErrorResponse {

    @Schema(description = "Response status", example = "error")
    private final String status;

    @Schema(description = "Application-specific error code", example = "ORDER_NOT_FOUND")
    private final String code;

    @Schema(description = "Human-readable error message", example = "Order with id 123 not found")
    private final String message;

    @Schema(description = "List of field-level validation errors")
    private final List<FieldError> details;

    @Schema(description = "Request path that caused the error", example = "/api/v1/orders/123")
    private final String path;

    @Schema(description = "ISO 8601 timestamp of the error", example = "2024-01-15T10:30:00Z")
    private final Instant timestamp;

    private ErrorResponse(String code, String message, List<FieldError> details, String path) {
        this.status = "error";
        this.code = code;
        this.message = message;
        this.details = details;
        this.path = path;
        this.timestamp = Instant.now();
    }

    /**
     * Creates an error response with a code and message.
     *
     * @param code    application-specific error code
     * @param message human-readable error message
     * @param path    the request path
     * @return a new {@link ErrorResponse}
     */
    public static ErrorResponse of(String code, String message, String path) {
        return new ErrorResponse(code, message, null, path);
    }

    /**
     * Creates an error response with validation details.
     *
     * @param code    application-specific error code
     * @param message human-readable error message
     * @param details list of field-level validation errors
     * @param path    the request path
     * @return a new {@link ErrorResponse}
     */
    public static ErrorResponse of(String code, String message, List<FieldError> details, String path) {
        return new ErrorResponse(code, message, details, path);
    }

    public String getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public List<FieldError> getDetails() {
        return details;
    }

    public String getPath() {
        return path;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    /**
     * Represents a single field-level validation error.
     */
    @Schema(description = "Field-level validation error")
    public static class FieldError {

        @Schema(description = "Field name that failed validation", example = "email")
        private final String field;

        @Schema(description = "Rejected value", example = "not-an-email")
        private final Object rejectedValue;

        @Schema(description = "Validation error message", example = "must be a valid email address")
        private final String message;

        public FieldError(String field, Object rejectedValue, String message) {
            this.field = field;
            this.rejectedValue = rejectedValue;
            this.message = message;
        }

        public String getField() {
            return field;
        }

        public Object getRejectedValue() {
            return rejectedValue;
        }

        public String getMessage() {
            return message;
        }
    }
}
