package net.chrisrichardson.ftgo.openapi.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

/**
 * Standardized error response format for all FTGO REST endpoints.
 *
 * <p>Example error response:
 * <pre>
 * {
 *   "status": "error",
 *   "code": 404,
 *   "message": "Order not found",
 *   "path": "/api/v1/orders/999",
 *   "errors": [
 *     {
 *       "field": "orderId",
 *       "message": "No order found with id 999"
 *     }
 *   ],
 *   "timestamp": "2024-01-15T10:30:00Z"
 * }
 * </pre>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard error response")
public class ApiError {

    @Schema(description = "Error status", example = "error")
    private String status;

    @Schema(description = "HTTP status code", example = "404")
    private int code;

    @Schema(description = "Human-readable error message", example = "Order not found")
    private String message;

    @Schema(description = "Request path that caused the error", example = "/api/v1/orders/999")
    private String path;

    @Schema(description = "Detailed field-level errors")
    private List<FieldError> errors;

    @Schema(description = "ISO 8601 timestamp", example = "2024-01-15T10:30:00Z")
    private Instant timestamp;

    public ApiError() {
        this.status = "error";
        this.timestamp = Instant.now();
    }

    public ApiError(int code, String message, String path) {
        this.status = "error";
        this.code = code;
        this.message = message;
        this.path = path;
        this.timestamp = Instant.now();
    }

    public ApiError(int code, String message, String path, List<FieldError> errors) {
        this(code, message, path);
        this.errors = errors;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<FieldError> getErrors() {
        return errors;
    }

    public void setErrors(List<FieldError> errors) {
        this.errors = errors;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Represents a field-level validation error.
     */
    @Schema(description = "Field-level validation error detail")
    public static class FieldError {

        @Schema(description = "Field name that failed validation", example = "consumerId")
        private String field;

        @Schema(description = "Validation error message", example = "must not be null")
        private String message;

        public FieldError() {
        }

        public FieldError(String field, String message) {
            this.field = field;
            this.message = message;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
