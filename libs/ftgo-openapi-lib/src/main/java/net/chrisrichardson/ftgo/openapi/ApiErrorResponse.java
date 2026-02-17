package net.chrisrichardson.ftgo.openapi;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

/**
 * Standard error response format for all FTGO microservices.
 * Ensures consistent error payloads across the platform.
 */
@Schema(description = "Standard API error response")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiErrorResponse {

    @Schema(description = "HTTP status code", example = "400")
    private int status;

    @Schema(description = "Error type", example = "VALIDATION_ERROR")
    private String error;

    @Schema(description = "Human-readable error message", example = "Invalid order request")
    private String message;

    @Schema(description = "ISO 8601 timestamp", example = "2024-01-15T10:30:00Z")
    private Instant timestamp;

    @Schema(description = "Request path", example = "/api/v1/orders")
    private String path;

    @Schema(description = "Field-level validation errors")
    private List<FieldError> fieldErrors;

    public ApiErrorResponse() {
        this.timestamp = Instant.now();
    }

    public ApiErrorResponse(int status, String error, String message, String path) {
        this();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    @Schema(description = "Individual field validation error")
    public static class FieldError {
        @Schema(description = "Field name", example = "consumerId")
        private String field;

        @Schema(description = "Rejection reason", example = "must not be null")
        private String message;

        public FieldError() {}

        public FieldError(String field, String message) {
            this.field = field;
            this.message = message;
        }

        public String getField() { return field; }
        public void setField(String field) { this.field = field; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    // Getters and setters
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public List<FieldError> getFieldErrors() { return fieldErrors; }
    public void setFieldErrors(List<FieldError> fieldErrors) { this.fieldErrors = fieldErrors; }
}
