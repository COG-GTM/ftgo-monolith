package com.ftgo.errorhandling.exception;

/**
 * Exception thrown when a business rule is violated.
 *
 * <p>Maps to HTTP 422 Unprocessable Entity. Use this for domain-level
 * validation failures such as order minimum not met, invalid quantities, etc.</p>
 *
 * <p>Example usage:</p>
 * <pre>
 * if (orderTotal.isLessThan(restaurant.getMinimumOrderAmount())) {
 *     throw new BusinessRuleException(
 *         ErrorCodes.ORDER_MINIMUM_NOT_MET,
 *         "Order total does not meet the minimum requirement"
 *     );
 * }
 * </pre>
 */
public class BusinessRuleException extends RuntimeException {

    private final String errorCode;

    public BusinessRuleException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public BusinessRuleException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
