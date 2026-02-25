package com.ftgo.common.error.code;

/**
 * Error codes specific to the Consumer domain.
 */
public enum ConsumerErrorCode implements ErrorCode {

    CONSUMER_NOT_FOUND("CONSUMER_NOT_FOUND", "The requested consumer was not found", 404),
    CONSUMER_ALREADY_EXISTS("CONSUMER_ALREADY_EXISTS", "A consumer with the given identifier already exists", 409),
    CONSUMER_VALIDATION_FAILED("CONSUMER_VALIDATION_FAILED", "Consumer data validation failed", 422);

    private final String code;
    private final String defaultMessage;
    private final int httpStatus;

    ConsumerErrorCode(String code, String defaultMessage, int httpStatus) {
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
