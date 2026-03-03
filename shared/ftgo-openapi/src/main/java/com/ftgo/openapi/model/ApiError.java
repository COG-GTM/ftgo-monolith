package com.ftgo.openapi.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Standard error model for API error responses.
 *
 * <p>Provides a consistent structure for reporting errors across all FTGO services,
 * including machine-readable error codes and optional field-level validation errors.
 *
 * <h3>Example JSON</h3>
 * <pre>
 * {
 *   "code": "VALIDATION_FAILED",
 *   "message": "Request validation failed",
 *   "details": "One or more fields have invalid values",
 *   "fieldErrors": [
 *     {
 *       "field": "deliveryAddress.zip",
 *       "message": "must not be blank",
 *       "rejectedValue": ""
 *     }
 *   ]
 * }
 * </pre>
 *
 * @see ApiResponse
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {

    /** Machine-readable error code (e.g., ORDER_NOT_FOUND, VALIDATION_FAILED). */
    private String code;

    /** Human-readable error message. */
    private String message;

    /** Additional details about the error. */
    private String details;

    /** Field-level validation errors, if applicable. */
    private List<FieldError> fieldErrors;

    public ApiError() {
    }

    public ApiError(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public ApiError(String code, String message, String details) {
        this.code = code;
        this.message = message;
        this.details = details;
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

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
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
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class FieldError {

        private String field;
        private String message;
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
