package com.ftgo.errorhandling.exception;

/**
 * Exception thrown when an operation conflicts with the current state of a resource.
 *
 * <p>Maps to HTTP 409 Conflict. Use this for state transition violations,
 * optimistic locking failures, or duplicate resource creation attempts.</p>
 *
 * <p>Example usage:</p>
 * <pre>
 * if (!order.canTransitionTo(newState)) {
 *     throw new ConflictException(
 *         ErrorCodes.ORDER_INVALID_STATE_TRANSITION,
 *         "Cannot transition order from " + order.getState() + " to " + newState
 *     );
 * }
 * </pre>
 */
public class ConflictException extends RuntimeException {

    private final String errorCode;

    public ConflictException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ConflictException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
