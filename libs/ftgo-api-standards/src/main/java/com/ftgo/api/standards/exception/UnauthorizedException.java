package com.ftgo.api.standards.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends BaseException {

    private static final String DEFAULT_CODE = "UNAUTHORIZED";

    public UnauthorizedException(String message) {
        super(message, DEFAULT_CODE, HttpStatus.UNAUTHORIZED);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause, DEFAULT_CODE, HttpStatus.UNAUTHORIZED);
    }
}
