package com.ftgo.common.error.code;

/**
 * Error codes specific to the Order domain.
 */
public enum OrderErrorCode implements ErrorCode {

    ORDER_NOT_FOUND("ORDER_NOT_FOUND", "The requested order was not found", 404),
    ORDER_MINIMUM_NOT_MET("ORDER_MINIMUM_NOT_MET", "Order total does not meet the minimum requirement", 422),
    ORDER_ALREADY_CANCELLED("ORDER_ALREADY_CANCELLED", "The order has already been cancelled", 409),
    ORDER_STATE_INVALID("ORDER_STATE_INVALID", "The order is not in a valid state for this operation", 409),
    ORDER_REVISION_REJECTED("ORDER_REVISION_REJECTED", "The order revision was rejected", 422),
    ORDER_LINE_ITEM_NOT_FOUND("ORDER_LINE_ITEM_NOT_FOUND", "The specified order line item was not found", 404);

    private final String code;
    private final String defaultMessage;
    private final int httpStatus;

    OrderErrorCode(String code, String defaultMessage, int httpStatus) {
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
