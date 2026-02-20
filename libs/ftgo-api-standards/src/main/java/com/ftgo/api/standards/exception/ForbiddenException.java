package com.ftgo.api.standards.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends BaseException {

    private static final String DEFAULT_CODE = "FORBIDDEN";

    public ForbiddenException(String message) {
        super(message, DEFAULT_CODE, HttpStatus.FORBIDDEN);
    }

    public ForbiddenException(String message, Throwable cause) {
        super(message, cause, DEFAULT_CODE, HttpStatus.FORBIDDEN);
    }
}
