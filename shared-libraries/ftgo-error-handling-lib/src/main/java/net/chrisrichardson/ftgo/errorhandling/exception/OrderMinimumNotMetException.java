package net.chrisrichardson.ftgo.errorhandling.exception;

import net.chrisrichardson.ftgo.errorhandling.ErrorCode;

/**
 * Thrown when an order total does not meet the restaurant's minimum.
 *
 * <p>This is the centralized equivalent of
 * {@code net.chrisrichardson.ftgo.domain.OrderMinimumNotMetException}.
 * Maps to HTTP 422 Unprocessable Entity.
 */
public class OrderMinimumNotMetException extends BusinessException {

    public OrderMinimumNotMetException() {
        super(ErrorCode.ORDER_MINIMUM_NOT_MET, "Order minimum not met");
    }

    public OrderMinimumNotMetException(String message) {
        super(ErrorCode.ORDER_MINIMUM_NOT_MET, message);
    }
}
