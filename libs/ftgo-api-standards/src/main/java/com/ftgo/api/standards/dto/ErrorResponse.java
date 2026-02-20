package com.ftgo.api.standards.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private int status;
    private String code;
    private String message;
    private String path;
    private String correlationId;
    private Instant timestamp;
    private List<FieldError> errors;

    public ErrorResponse() {
        this.timestamp = Instant.now();
        this.errors = new ArrayList<>();
    }

    private ErrorResponse(int status, String code, String message, String path, String correlationId) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.path = path;
        this.correlationId = correlationId;
        this.timestamp = Instant.now();
        this.errors = new ArrayList<>();
    }

    public static ErrorResponse of(int status, String code, String message) {
        return new ErrorResponse(status, code, message, null, null);
    }

    public static ErrorResponse of(int status, String code, String message, String path) {
        return new ErrorResponse(status, code, message, path, null);
    }

    public static ErrorResponse of(int status, String code, String message, String path, String correlationId) {
        return new ErrorResponse(status, code, message, path, correlationId);
    }

    public ErrorResponse addFieldError(String field, String message) {
        this.errors.add(new FieldError(field, message));
        return this;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public List<FieldError> getErrors() {
        return errors;
    }

    public void setErrors(List<FieldError> errors) {
        this.errors = errors;
    }

    public static class FieldError {

        private String field;
        private String message;

        public FieldError() {
        }

        public FieldError(String field, String message) {
            this.field = field;
            this.message = message;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
