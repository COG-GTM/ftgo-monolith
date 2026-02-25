package com.ftgo.common.error.response;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Represents a single validation or field-level error detail
 * within an {@link ErrorResponse}.
 *
 * <p>Used primarily for Bean Validation errors to provide
 * field-level error information to API consumers.</p>
 *
 * <h3>Example JSON</h3>
 * <pre>
 * {
 *   "field": "email",
 *   "message": "must not be blank",
 *   "rejectedValue": ""
 * }
 * </pre>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorDetail {

    private String field;
    private String message;
    private Object rejectedValue;

    /**
     * Default constructor for Jackson deserialization.
     */
    public ErrorDetail() {
    }

    public ErrorDetail(String field, String message, Object rejectedValue) {
        this.field = field;
        this.message = message;
        this.rejectedValue = rejectedValue;
    }

    public String getField() {
        return field;
    }

    public String getMessage() {
        return message;
    }

    public Object getRejectedValue() {
        return rejectedValue;
    }

    /**
     * Creates a new {@link ErrorDetail} for a field validation error.
     *
     * @param field         the field name that failed validation
     * @param message       the validation error message
     * @param rejectedValue the value that was rejected
     * @return a new {@link ErrorDetail}
     */
    public static ErrorDetail of(String field, String message, Object rejectedValue) {
        return new ErrorDetail(field, message, rejectedValue);
    }

    /**
     * Creates a new {@link ErrorDetail} for a general error detail without a rejected value.
     *
     * @param field   the field name
     * @param message the error message
     * @return a new {@link ErrorDetail}
     */
    public static ErrorDetail of(String field, String message) {
        return new ErrorDetail(field, message, null);
    }
}
