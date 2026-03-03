package com.ftgo.errorhandling.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Standardized error response DTO returned by all FTGO microservices.
 *
 * <p>All error responses follow this consistent JSON format:</p>
 * <pre>
 * {
 *   "code": "FTGO-01001",
 *   "message": "Validation failed",
 *   "details": "One or more fields failed validation",
 *   "timestamp": "2026-03-03T02:30:00Z",
 *   "traceId": "abc123def456",
 *   "path": "/orders",
 *   "fieldErrors": [
 *     {
 *       "field": "consumerId",
 *       "message": "must not be null",
 *       "rejectedValue": null
 *     }
 *   ]
 * }
 * </pre>
 *
 * <p>The {@code fieldErrors} array is only included when there are validation errors.
 * The {@code traceId} is populated from the distributed tracing context when available.</p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    /** FTGO error code (e.g., FTGO-01001). */
    @JsonProperty("code")
    private String code;

    /** Human-readable error message. */
    @JsonProperty("message")
    private String message;

    /** Additional error details or context. */
    @JsonProperty("details")
    private String details;

    /** ISO-8601 timestamp of when the error occurred. */
    @JsonProperty("timestamp")
    private String timestamp;

    /** Distributed trace ID for correlating logs and requests. */
    @JsonProperty("traceId")
    private String traceId;

    /** Request path that triggered the error. */
    @JsonProperty("path")
    private String path;

    /** HTTP status code. */
    @JsonProperty("status")
    private int status;

    /** Field-level validation errors (only present for validation failures). */
    @JsonProperty("fieldErrors")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<FieldError> fieldErrors;

    public ErrorResponse() {
        this.timestamp = Instant.now().toString();
        this.fieldErrors = new ArrayList<>();
    }

    public ErrorResponse(String code, String message, int status) {
        this();
        this.code = code;
        this.message = message;
        this.status = status;
    }

    public ErrorResponse(String code, String message, String details, int status) {
        this(code, message, status);
        this.details = details;
    }

    // -------------------------------------------------------------------------
    // Getters and Setters
    // -------------------------------------------------------------------------

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

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public List<FieldError> getFieldErrors() {
        return Collections.unmodifiableList(fieldErrors);
    }

    public void setFieldErrors(List<FieldError> fieldErrors) {
        this.fieldErrors = fieldErrors != null ? new ArrayList<>(fieldErrors) : new ArrayList<>();
    }

    public void addFieldError(String field, String message, Object rejectedValue) {
        this.fieldErrors.add(new FieldError(field, message, rejectedValue));
    }

    // -------------------------------------------------------------------------
    // Builder-style methods for fluent construction
    // -------------------------------------------------------------------------

    public ErrorResponse withDetails(String details) {
        this.details = details;
        return this;
    }

    public ErrorResponse withTraceId(String traceId) {
        this.traceId = traceId;
        return this;
    }

    public ErrorResponse withPath(String path) {
        this.path = path;
        return this;
    }

    public ErrorResponse withFieldError(String field, String message, Object rejectedValue) {
        addFieldError(field, message, rejectedValue);
        return this;
    }

    // -------------------------------------------------------------------------
    // Nested FieldError class for validation error details
    // -------------------------------------------------------------------------

    /**
     * Represents a single field-level validation error.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class FieldError {

        /** The field name that failed validation. */
        @JsonProperty("field")
        private String field;

        /** The validation error message. */
        @JsonProperty("message")
        private String message;

        /** The rejected value that caused the validation failure. */
        @JsonProperty("rejectedValue")
        private Object rejectedValue;

        public FieldError() {
        }

        public FieldError(String field, String message, Object rejectedValue) {
            this.field = field;
            this.message = message;
            this.rejectedValue = rejectedValue;
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

        public Object getRejectedValue() {
            return rejectedValue;
        }

        public void setRejectedValue(Object rejectedValue) {
            this.rejectedValue = rejectedValue;
        }
    }
}
