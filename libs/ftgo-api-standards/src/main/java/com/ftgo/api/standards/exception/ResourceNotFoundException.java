package com.ftgo.api.standards.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BaseException {

    private static final String DEFAULT_CODE = "NOT_FOUND";

    public ResourceNotFoundException(String message) {
        super(message, DEFAULT_CODE, HttpStatus.NOT_FOUND);
    }

    public ResourceNotFoundException(String resourceType, Object resourceId) {
        super(resourceType + " not found with id: " + resourceId, DEFAULT_CODE, HttpStatus.NOT_FOUND);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause, DEFAULT_CODE, HttpStatus.NOT_FOUND);
    }
}
