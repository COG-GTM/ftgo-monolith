package com.ftgo.api.standards.exception;

import org.springframework.http.HttpStatus;

public class BusinessException extends BaseException {

    private static final String DEFAULT_CODE = "UNPROCESSABLE_ENTITY";

    public BusinessException(String message) {
        super(message, DEFAULT_CODE, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    public BusinessException(String message, String errorCode) {
        super(message, errorCode, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause, DEFAULT_CODE, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    public BusinessException(String message, String errorCode, Throwable cause) {
        super(message, cause, errorCode, HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
