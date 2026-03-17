package net.chrisrichardson.ftgo.openapi.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

/**
 * Standardized error response format for all FTGO REST APIs.
 *
 * <p>All error responses follow this envelope format, providing a consistent
 * structure for API consumers to handle errors uniformly.</p>
 */
@Schema(description = "Standard error response envelope for all FTGO APIs")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiErrorResponse {

    @Schema(description = "HTTP status code", example = "404")
    private int status;

    @Schema(description = "Short error code identifier", example = "NOT_FOUND")
    private String error;

    @Schema(description = "Human-readable error message", example = "Order not found")
    private String message;

    @Schema(description = "Request path that caused the error", example = "/api/v1/orders/123")
    private String path;

    @Schema(description = "Timestamp of the error in ISO 8601 format")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant timestamp;

    @Schema(description = "Detailed validation errors, if applicable")
    private List<FieldError> fieldErrors;

    public ApiErrorResponse() {
        this.timestamp = Instant.now();
    }

    public ApiErrorResponse(int status, String error, String message, String path) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.timestamp = Instant.now();
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
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

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public List<FieldError> getFieldErrors() {
        return fieldErrors;
    }

    public void setFieldErrors(List<FieldError> fieldErrors) {
        this.fieldErrors = fieldErrors;
    }

    /**
     * Represents a single field-level validation error.
     */
    @Schema(description = "Individual field validation error")
    public static class FieldError {

        @Schema(description = "Field name that failed validation", example = "consumerId")
        private String field;

        @Schema(description = "Rejected value", example = "null")
        private Object rejectedValue;

        @Schema(description = "Validation error message", example = "must not be null")
        private String message;

        public FieldError() {
        }

        public FieldError(String field, Object rejectedValue, String message) {
            this.field = field;
            this.rejectedValue = rejectedValue;
            this.message = message;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public Object getRejectedValue() {
            return rejectedValue;
        }

        public void setRejectedValue(Object rejectedValue) {
            this.rejectedValue = rejectedValue;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
