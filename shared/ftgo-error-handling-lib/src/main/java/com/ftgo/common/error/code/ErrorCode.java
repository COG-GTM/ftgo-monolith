package com.ftgo.common.error.code;

/**
 * Interface for error codes across all FTGO domains.
 *
 * <p>Each domain defines its own enum implementing this interface,
 * providing domain-specific error codes with consistent behavior.</p>
 *
 * @see CommonErrorCode
 * @see OrderErrorCode
 * @see ConsumerErrorCode
 * @see RestaurantErrorCode
 * @see CourierErrorCode
 */
public interface ErrorCode {

    /**
     * Returns the unique error code string (e.g., "ORDER_NOT_FOUND").
     *
     * @return the error code identifier
     */
    String getCode();

    /**
     * Returns a human-readable default message for this error.
     *
     * @return the default error message
     */
    String getDefaultMessage();

    /**
     * Returns the HTTP status code associated with this error.
     *
     * @return the HTTP status code
     */
    int getHttpStatus();
}
