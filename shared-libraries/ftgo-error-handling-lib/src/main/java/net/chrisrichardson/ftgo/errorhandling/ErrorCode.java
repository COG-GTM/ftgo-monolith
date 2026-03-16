package net.chrisrichardson.ftgo.errorhandling;

/**
 * Standard machine-readable error codes for FTGO APIs.
 *
 * <p>Each code maps to a specific category of error. The {@link #getHttpStatus()}
 * method returns the recommended HTTP status code for the error category.
 */
public enum ErrorCode {

    // --- Generic ---
    INTERNAL_ERROR("INTERNAL_ERROR", "An unexpected internal error occurred", 500),
    BAD_REQUEST("BAD_REQUEST", "The request was malformed or invalid", 400),
    UNAUTHORIZED("UNAUTHORIZED", "Authentication is required", 401),
    FORBIDDEN("FORBIDDEN", "You do not have permission to perform this action", 403),

    // --- Resource ---
    RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND", "The requested resource was not found", 404),
    RESOURCE_ALREADY_EXISTS("RESOURCE_ALREADY_EXISTS", "The resource already exists", 409),

    // --- Validation ---
    VALIDATION_FAILED("VALIDATION_FAILED", "Request validation failed", 400),

    // --- Business logic ---
    BUSINESS_RULE_VIOLATION("BUSINESS_RULE_VIOLATION", "A business rule was violated", 422),
    UNSUPPORTED_STATE_TRANSITION("UNSUPPORTED_STATE_TRANSITION", "The requested state transition is not allowed", 409),
    ORDER_MINIMUM_NOT_MET("ORDER_MINIMUM_NOT_MET", "The order minimum was not met", 422),

    // --- Concurrency ---
    OPTIMISTIC_LOCK_CONFLICT("OPTIMISTIC_LOCK_CONFLICT", "The resource was modified by another request", 409),

    // --- External service ---
    SERVICE_UNAVAILABLE("SERVICE_UNAVAILABLE", "A required service is unavailable", 503);

    private final String code;
    private final String defaultMessage;
    private final int httpStatus;

    ErrorCode(String code, String defaultMessage, int httpStatus) {
        this.code = code;
        this.defaultMessage = defaultMessage;
        this.httpStatus = httpStatus;
    }

    public String getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
