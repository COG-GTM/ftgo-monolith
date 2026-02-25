package com.ftgo.common.error.code;

/**
 * Common error codes shared across all FTGO microservices.
 *
 * <p>These codes cover generic errors such as validation failures,
 * authentication errors, and internal server errors.</p>
 */
public enum CommonErrorCode implements ErrorCode {

    // --- Validation Errors (400) ---
    VALIDATION_ERROR("VALIDATION_ERROR", "Request validation failed", 400),
    INVALID_REQUEST("INVALID_REQUEST", "The request is malformed or contains invalid data", 400),
    MISSING_REQUIRED_FIELD("MISSING_REQUIRED_FIELD", "A required field is missing", 400),

    // --- Authentication Errors (401) ---
    UNAUTHORIZED("UNAUTHORIZED", "Authentication is required to access this resource", 401),
    INVALID_CREDENTIALS("INVALID_CREDENTIALS", "The provided credentials are invalid", 401),
    TOKEN_EXPIRED("TOKEN_EXPIRED", "The authentication token has expired", 401),

    // --- Authorization Errors (403) ---
    ACCESS_DENIED("ACCESS_DENIED", "You do not have permission to access this resource", 403),
    INSUFFICIENT_PERMISSIONS("INSUFFICIENT_PERMISSIONS", "Insufficient permissions for this operation", 403),

    // --- Not Found Errors (404) ---
    RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND", "The requested resource was not found", 404),

    // --- Conflict Errors (409) ---
    STATE_TRANSITION_ERROR("STATE_TRANSITION_ERROR", "The requested state transition is not allowed", 409),
    RESOURCE_CONFLICT("RESOURCE_CONFLICT", "The request conflicts with the current state of the resource", 409),
    OPTIMISTIC_LOCK_ERROR("OPTIMISTIC_LOCK_ERROR", "The resource was modified by another request", 409),

    // --- Unprocessable Entity Errors (422) ---
    BUSINESS_RULE_VIOLATION("BUSINESS_RULE_VIOLATION", "A business rule was violated", 422),

    // --- Too Many Requests (429) ---
    RATE_LIMIT_EXCEEDED("RATE_LIMIT_EXCEEDED", "Too many requests. Please try again later", 429),

    // --- Internal Errors (500) ---
    INTERNAL_ERROR("INTERNAL_ERROR", "An unexpected internal error occurred", 500),
    NOT_YET_IMPLEMENTED("NOT_YET_IMPLEMENTED", "This feature is not yet implemented", 501),

    // --- Service Communication Errors (502/503) ---
    SERVICE_UNAVAILABLE("SERVICE_UNAVAILABLE", "The service is temporarily unavailable", 503),
    UPSTREAM_SERVICE_ERROR("UPSTREAM_SERVICE_ERROR", "An upstream service returned an error", 502),
    SERVICE_TIMEOUT("SERVICE_TIMEOUT", "The request to an upstream service timed out", 504);

    private final String code;
    private final String defaultMessage;
    private final int httpStatus;

    CommonErrorCode(String code, String defaultMessage, int httpStatus) {
        this.code = code;
        this.defaultMessage = defaultMessage;
        this.httpStatus = httpStatus;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getDefaultMessage() {
        return defaultMessage;
    }

    @Override
    public int getHttpStatus() {
        return httpStatus;
    }
}
