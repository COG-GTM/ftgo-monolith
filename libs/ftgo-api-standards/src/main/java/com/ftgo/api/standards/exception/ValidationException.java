package com.ftgo.api.standards.exception;

import java.util.Collections;
import java.util.Map;
import org.springframework.http.HttpStatus;

public class ValidationException extends BaseException {

    private static final String DEFAULT_CODE = "VALIDATION_ERROR";
    private final Map<String, String> fieldErrors;

    public ValidationException(String message) {
        super(message, DEFAULT_CODE, HttpStatus.BAD_REQUEST);
        this.fieldErrors = Collections.emptyMap();
    }

    public ValidationException(String message, Map<String, String> fieldErrors) {
        super(message, DEFAULT_CODE, HttpStatus.BAD_REQUEST);
        this.fieldErrors = fieldErrors != null ? Collections.unmodifiableMap(fieldErrors) : Collections.emptyMap();
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause, DEFAULT_CODE, HttpStatus.BAD_REQUEST);
        this.fieldErrors = Collections.emptyMap();
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
}
