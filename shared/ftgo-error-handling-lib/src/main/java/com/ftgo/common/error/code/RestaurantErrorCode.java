package com.ftgo.common.error.code;

/**
 * Error codes specific to the Restaurant domain.
 */
public enum RestaurantErrorCode implements ErrorCode {

    RESTAURANT_NOT_FOUND("RESTAURANT_NOT_FOUND", "The requested restaurant was not found", 404),
    RESTAURANT_CLOSED("RESTAURANT_CLOSED", "The restaurant is currently closed", 422),
    MENU_ITEM_NOT_FOUND("MENU_ITEM_NOT_FOUND", "The specified menu item was not found", 404),
    RESTAURANT_ALREADY_EXISTS("RESTAURANT_ALREADY_EXISTS", "A restaurant with the given identifier already exists", 409);

    private final String code;
    private final String defaultMessage;
    private final int httpStatus;

    RestaurantErrorCode(String code, String defaultMessage, int httpStatus) {
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
