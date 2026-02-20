package com.ftgo.api.standards.exception;

import org.springframework.http.HttpStatus;

public class ServiceUnavailableException extends BaseException {

    private static final String DEFAULT_CODE = "SERVICE_UNAVAILABLE";

    public ServiceUnavailableException(String message) {
        super(message, DEFAULT_CODE, HttpStatus.SERVICE_UNAVAILABLE);
    }

    public ServiceUnavailableException(String message, Throwable cause) {
        super(message, cause, DEFAULT_CODE, HttpStatus.SERVICE_UNAVAILABLE);
    }
}
