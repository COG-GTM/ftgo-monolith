package com.ftgo.api.standards.exception;

import org.springframework.http.HttpStatus;

public class UpstreamServiceException extends BaseException {

    private static final String DEFAULT_CODE = "UPSTREAM_ERROR";

    public UpstreamServiceException(String message) {
        super(message, DEFAULT_CODE, HttpStatus.BAD_GATEWAY);
    }

    public UpstreamServiceException(String message, Throwable cause) {
        super(message, cause, DEFAULT_CODE, HttpStatus.BAD_GATEWAY);
    }
}
