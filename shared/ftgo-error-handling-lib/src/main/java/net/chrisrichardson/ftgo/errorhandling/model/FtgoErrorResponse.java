package net.chrisrichardson.ftgo.errorhandling.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

/**
 * Standardized error response DTO for all FTGO microservices.
 *
 * <p>Every error returned by FTGO REST endpoints uses this envelope to
 * ensure a consistent format across all services. The envelope includes:</p>
 * <ul>
 *     <li>{@code status} — Always "error"</li>
 *     <li>{@code code} — Application-specific error code from {@link net.chrisrichardson.ftgo.errorhandling.constants.FtgoErrorCodes}</li>
 *     <li>{@code message} — Human-readable error message</li>
 *     <li>{@code details} — Optional list of field-level validation errors</li>
 *     <li>{@code path} — The request path that caused the error</li>
 *     <li>{@code timestamp} — ISO 8601 timestamp of the error</li>
 *     <li>{@code traceId} — Distributed tracing identifier for log correlation</li>
 * </ul>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard error response envelope for FTGO services")
public class FtgoErrorResponse {

    @Schema(description = "Response status — always 'error'", example = "error")
    private final String status;

    @Schema(description = "Application-specific error code", example = "ORD_NOT_FOUND")
    private final String code;

    @Schema(description = "Human-readable error message", example = "Order with id 123 not found")
    private final String message;

    @Schema(description = "List of field-level validation errors")
    private final List<FieldError> details;

    @Schema(description = "Request path that caused the error", example = "/api/v1/orders/123")
    private final String path;

    @Schema(description = "ISO 8601 timestamp of the error", example = "2024-01-15T10:30:00Z")
    private final Instant timestamp;

    @Schema(description = "Distributed trace ID for log correlation", example = "6a3e94b234f9a1c2")
    private final String traceId;

    private FtgoErrorResponse(String code, String message, List<FieldError> details,
                              String path, String traceId) {
        this.status = "error";
        this.code = code;
        this.message = message;
        this.details = details;
        this.path = path;
        this.timestamp = Instant.now();
        this.traceId = traceId;
    }

    /**
     * Creates an error response without validation details.
     *
     * @param code    application-specific error code
     * @param message human-readable error message
     * @param path    the request path
     * @param traceId distributed trace ID (may be null)
     * @return a new {@link FtgoErrorResponse}
     */
    public static FtgoErrorResponse of(String code, String message, String path, String traceId) {
        return new FtgoErrorResponse(code, message, null, path, traceId);
    }

    /**
     * Creates an error response with validation details.
     *
     * @param code    application-specific error code
     * @param message human-readable error message
     * @param details list of field-level validation errors
     * @param path    the request path
     * @param traceId distributed trace ID (may be null)
     * @return a new {@link FtgoErrorResponse}
     */
    public static FtgoErrorResponse of(String code, String message, List<FieldError> details,
                                       String path, String traceId) {
        return new FtgoErrorResponse(code, message, details, path, traceId);
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

    public String getTraceId() {
        return traceId;
    }

    /**
     * Represents a single field-level validation error.
     */
    @Schema(description = "Field-level validation error detail")
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
