package com.ftgo.common.error.code;

/**
 * Error codes specific to the Courier domain.
 */
public enum CourierErrorCode implements ErrorCode {

    COURIER_NOT_FOUND("COURIER_NOT_FOUND", "The requested courier was not found", 404),
    COURIER_UNAVAILABLE("COURIER_UNAVAILABLE", "The courier is currently unavailable", 422),
    DELIVERY_NOT_FOUND("DELIVERY_NOT_FOUND", "The requested delivery was not found", 404),
    DELIVERY_ALREADY_ASSIGNED("DELIVERY_ALREADY_ASSIGNED", "The delivery is already assigned to a courier", 409);

    private final String code;
    private final String defaultMessage;
    private final int httpStatus;

    CourierErrorCode(String code, String defaultMessage, int httpStatus) {
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
