package com.ftgo.api.standards.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends BaseException {

    private static final String DEFAULT_CODE = "CONFLICT";

    public ConflictException(String message) {
        super(message, DEFAULT_CODE, HttpStatus.CONFLICT);
    }

    public ConflictException(String message, Throwable cause) {
        super(message, cause, DEFAULT_CODE, HttpStatus.CONFLICT);
    }
}
