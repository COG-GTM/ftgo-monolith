package net.chrisrichardson.ftgo.openapi.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Standard error response body for all FTGO REST APIs.
 *
 * <p>Returned whenever an API request results in an error (4xx or 5xx):
 * <pre>
 * {
 *   "code": "VALIDATION_FAILED",
 *   "message": "Request validation failed",
 *   "timestamp": "2026-03-16T20:00:00Z",
 *   "path": "/api/v1/orders",
 *   "details": [
 *     { "field": "consumerId", "message": "must not be null", "code": "NotNull" }
 *   ]
 * }
 * </pre>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard error response")
public class ErrorResponse {

    @Schema(description = "Machine-readable error code", example = "VALIDATION_FAILED")
    private String code;

    @Schema(description = "Human-readable error message", example = "Request validation failed")
    private String message;

    @Schema(description = "ISO-8601 timestamp of the error", example = "2026-03-16T20:00:00Z")
    private String timestamp;

    @Schema(description = "Request path that caused the error", example = "/api/v1/orders")
    private String path;

    @Schema(description = "Detailed error information (e.g., field validation errors)")
    private List<ErrorDetail> details;

    public ErrorResponse() {
        this.timestamp = Instant.now().toString();
    }

    public ErrorResponse(String code, String message) {
        this.code = code;
        this.message = message;
        this.timestamp = Instant.now().toString();
    }

    public ErrorResponse(String code, String message, String path) {
        this.code = code;
        this.message = message;
        this.path = path;
        this.timestamp = Instant.now().toString();
    }

    /**
     * Convenience builder for creating error responses with details.
     */
    public static Builder builder() {
        return new Builder();
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<ErrorDetail> getDetails() {
        return details;
    }

    public void setDetails(List<ErrorDetail> details) {
        this.details = details;
    }

    /**
     * Fluent builder for {@link ErrorResponse}.
     */
    public static class Builder {
        private String code;
        private String message;
        private String path;
        private List<ErrorDetail> details;

        public Builder code(String code) {
            this.code = code;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder detail(String field, String message, String code) {
            if (this.details == null) {
                this.details = new ArrayList<>();
            }
            this.details.add(new ErrorDetail(field, message, code));
            return this;
        }

        public Builder details(List<ErrorDetail> details) {
            this.details = details;
            return this;
        }

        public ErrorResponse build() {
            ErrorResponse response = new ErrorResponse(code, message, path);
            if (details != null) {
                response.setDetails(Collections.unmodifiableList(details));
            }
            return response;
        }
    }
}
