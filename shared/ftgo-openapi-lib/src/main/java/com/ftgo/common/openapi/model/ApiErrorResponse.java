package com.ftgo.common.openapi.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Standard error response format for all FTGO microservice endpoints.
 *
 * <p>All error responses (4xx, 5xx) should follow this structure to provide
 * consistent error handling across the platform. Example:
 *
 * <pre>
 * {
 *   "status": "error",
 *   "error": {
 *     "code": "RESOURCE_NOT_FOUND",
 *     "message": "Order not found",
 *     "details": [
 *       {
 *         "field": "orderId",
 *         "message": "No order exists with ID 999"
 *       }
 *     ]
 *   },
 *   "timestamp": "2024-01-15T10:30:00Z",
 *   "path": "/api/v1/orders/999"
 * }
 * </pre>
 *
 * @see ApiResponse
 */
@Schema(description = "Standard API error response")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiErrorResponse {

    @Schema(description = "Response status indicator", example = "error", requiredMode = Schema.RequiredMode.REQUIRED)
    private String status;

    @Schema(description = "Error details", requiredMode = Schema.RequiredMode.REQUIRED)
    private ErrorBody error;

    @Schema(description = "ISO 8601 timestamp of the error", example = "2024-01-15T10:30:00Z")
    private Instant timestamp;

    @Schema(description = "Request path that generated this error", example = "/api/v1/orders/999")
    private String path;

    public ApiErrorResponse() {
        this.status = "error";
        this.timestamp = Instant.now();
    }

    public ApiErrorResponse(String code, String message, String path) {
        this.status = "error";
        this.error = new ErrorBody(code, message);
        this.timestamp = Instant.now();
        this.path = path;
    }

    /**
     * Creates an error response with the given code, message, and path.
     *
     * @param code    machine-readable error code (e.g., "RESOURCE_NOT_FOUND")
     * @param message human-readable error message
     * @param path    the request path
     * @return a new ApiErrorResponse
     */
    public static ApiErrorResponse of(String code, String message, String path) {
        return new ApiErrorResponse(code, message, path);
    }

    /**
     * Adds a field-level validation error detail.
     *
     * @param field   the field name that failed validation
     * @param message the validation error message
     * @return this response for fluent chaining
     */
    public ApiErrorResponse addDetail(String field, String message) {
        if (this.error == null) {
            this.error = new ErrorBody();
        }
        this.error.addDetail(new FieldError(field, message));
        return this;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ErrorBody getError() {
        return error;
    }

    public void setError(ErrorBody error) {
        this.error = error;
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

    /**
     * The error body containing the error code, message, and optional field-level details.
     */
    @Schema(description = "Error details body")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ErrorBody {

        @Schema(description = "Machine-readable error code", example = "RESOURCE_NOT_FOUND",
                requiredMode = Schema.RequiredMode.REQUIRED)
        private String code;

        @Schema(description = "Human-readable error message", example = "Order not found",
                requiredMode = Schema.RequiredMode.REQUIRED)
        private String message;

        @Schema(description = "Field-level validation error details")
        private List<FieldError> details;

        public ErrorBody() {
        }

        public ErrorBody(String code, String message) {
            this.code = code;
            this.message = message;
        }

        public void addDetail(FieldError fieldError) {
            if (this.details == null) {
                this.details = new ArrayList<>();
            }
            this.details.add(fieldError);
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

        public List<FieldError> getDetails() {
            return details;
        }

        public void setDetails(List<FieldError> details) {
            this.details = details;
        }
    }

    /**
     * Represents a single field-level validation error.
     */
    @Schema(description = "Field-level validation error")
    public static class FieldError {

        @Schema(description = "Field name that failed validation", example = "orderId")
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
