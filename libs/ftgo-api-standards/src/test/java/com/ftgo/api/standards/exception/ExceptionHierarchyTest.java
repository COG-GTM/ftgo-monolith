package com.ftgo.api.standards.exception;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class ExceptionHierarchyTest {

    @Test
    void businessExceptionDefaults() {
        BusinessException ex = new BusinessException("Order cannot be cancelled");

        assertThat(ex.getMessage()).isEqualTo("Order cannot be cancelled");
        assertThat(ex.getErrorCode()).isEqualTo("UNPROCESSABLE_ENTITY");
        assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void businessExceptionWithCustomCode() {
        BusinessException ex = new BusinessException("Insufficient funds", "INSUFFICIENT_FUNDS");

        assertThat(ex.getErrorCode()).isEqualTo("INSUFFICIENT_FUNDS");
        assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void businessExceptionWithCause() {
        RuntimeException cause = new RuntimeException("root cause");
        BusinessException ex = new BusinessException("Order failed", cause);

        assertThat(ex.getCause()).isEqualTo(cause);
        assertThat(ex.getErrorCode()).isEqualTo("UNPROCESSABLE_ENTITY");
    }

    @Test
    void businessExceptionWithCustomCodeAndCause() {
        RuntimeException cause = new RuntimeException("root cause");
        BusinessException ex = new BusinessException("Order failed", "CUSTOM_CODE", cause);

        assertThat(ex.getCause()).isEqualTo(cause);
        assertThat(ex.getErrorCode()).isEqualTo("CUSTOM_CODE");
    }

    @Test
    void resourceNotFoundExceptionDefaults() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Order not found");

        assertThat(ex.getMessage()).isEqualTo("Order not found");
        assertThat(ex.getErrorCode()).isEqualTo("NOT_FOUND");
        assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void resourceNotFoundExceptionWithResourceTypeAndId() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Order", 42L);

        assertThat(ex.getMessage()).isEqualTo("Order not found with id: 42");
        assertThat(ex.getErrorCode()).isEqualTo("NOT_FOUND");
    }

    @Test
    void resourceNotFoundExceptionWithCause() {
        RuntimeException cause = new RuntimeException("db error");
        ResourceNotFoundException ex = new ResourceNotFoundException("Order not found", cause);

        assertThat(ex.getCause()).isEqualTo(cause);
    }

    @Test
    void validationExceptionDefaults() {
        ValidationException ex = new ValidationException("Validation failed");

        assertThat(ex.getMessage()).isEqualTo("Validation failed");
        assertThat(ex.getErrorCode()).isEqualTo("VALIDATION_ERROR");
        assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ex.getFieldErrors()).isEmpty();
    }

    @Test
    void validationExceptionWithFieldErrors() {
        Map<String, String> fieldErrors = Map.of("email", "must be valid", "name", "must not be blank");
        ValidationException ex = new ValidationException("Validation failed", fieldErrors);

        assertThat(ex.getFieldErrors()).hasSize(2);
        assertThat(ex.getFieldErrors()).containsEntry("email", "must be valid");
        assertThat(ex.getFieldErrors()).containsEntry("name", "must not be blank");
    }

    @Test
    void validationExceptionWithNullFieldErrors() {
        Map<String, String> nullMap = null;
        ValidationException ex = new ValidationException("Validation failed", nullMap);

        assertThat(ex.getFieldErrors()).isEmpty();
    }

    @Test
    void validationExceptionWithCause() {
        RuntimeException cause = new RuntimeException("parse error");
        ValidationException ex = new ValidationException("Validation failed", cause);

        assertThat(ex.getCause()).isEqualTo(cause);
        assertThat(ex.getFieldErrors()).isEmpty();
    }

    @Test
    void conflictExceptionDefaults() {
        ConflictException ex = new ConflictException("Resource already exists");

        assertThat(ex.getMessage()).isEqualTo("Resource already exists");
        assertThat(ex.getErrorCode()).isEqualTo("CONFLICT");
        assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void conflictExceptionWithCause() {
        RuntimeException cause = new RuntimeException("duplicate key");
        ConflictException ex = new ConflictException("Resource already exists", cause);

        assertThat(ex.getCause()).isEqualTo(cause);
    }

    @Test
    void unauthorizedExceptionDefaults() {
        UnauthorizedException ex = new UnauthorizedException("Invalid token");

        assertThat(ex.getMessage()).isEqualTo("Invalid token");
        assertThat(ex.getErrorCode()).isEqualTo("UNAUTHORIZED");
        assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void unauthorizedExceptionWithCause() {
        RuntimeException cause = new RuntimeException("token expired");
        UnauthorizedException ex = new UnauthorizedException("Invalid token", cause);

        assertThat(ex.getCause()).isEqualTo(cause);
    }

    @Test
    void forbiddenExceptionDefaults() {
        ForbiddenException ex = new ForbiddenException("Insufficient permissions");

        assertThat(ex.getMessage()).isEqualTo("Insufficient permissions");
        assertThat(ex.getErrorCode()).isEqualTo("FORBIDDEN");
        assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void forbiddenExceptionWithCause() {
        RuntimeException cause = new RuntimeException("role missing");
        ForbiddenException ex = new ForbiddenException("Insufficient permissions", cause);

        assertThat(ex.getCause()).isEqualTo(cause);
    }

    @Test
    void serviceUnavailableExceptionDefaults() {
        ServiceUnavailableException ex = new ServiceUnavailableException("Service is down");

        assertThat(ex.getMessage()).isEqualTo("Service is down");
        assertThat(ex.getErrorCode()).isEqualTo("SERVICE_UNAVAILABLE");
        assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    }

    @Test
    void serviceUnavailableExceptionWithCause() {
        RuntimeException cause = new RuntimeException("connection refused");
        ServiceUnavailableException ex = new ServiceUnavailableException("Service is down", cause);

        assertThat(ex.getCause()).isEqualTo(cause);
    }

    @Test
    void upstreamServiceExceptionDefaults() {
        UpstreamServiceException ex = new UpstreamServiceException("Payment service failed");

        assertThat(ex.getMessage()).isEqualTo("Payment service failed");
        assertThat(ex.getErrorCode()).isEqualTo("UPSTREAM_ERROR");
        assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.BAD_GATEWAY);
    }

    @Test
    void upstreamServiceExceptionWithCause() {
        RuntimeException cause = new RuntimeException("timeout");
        UpstreamServiceException ex = new UpstreamServiceException("Payment service failed", cause);

        assertThat(ex.getCause()).isEqualTo(cause);
    }

    @Test
    void allExceptionsExtendBaseException() {
        assertThat(new BusinessException("test")).isInstanceOf(BaseException.class);
        assertThat(new ResourceNotFoundException("test")).isInstanceOf(BaseException.class);
        assertThat(new ValidationException("test")).isInstanceOf(BaseException.class);
        assertThat(new ConflictException("test")).isInstanceOf(BaseException.class);
        assertThat(new UnauthorizedException("test")).isInstanceOf(BaseException.class);
        assertThat(new ForbiddenException("test")).isInstanceOf(BaseException.class);
        assertThat(new ServiceUnavailableException("test")).isInstanceOf(BaseException.class);
        assertThat(new UpstreamServiceException("test")).isInstanceOf(BaseException.class);
    }

    @Test
    void allExceptionsAreRuntimeExceptions() {
        assertThat(new BusinessException("test")).isInstanceOf(RuntimeException.class);
        assertThat(new ResourceNotFoundException("test")).isInstanceOf(RuntimeException.class);
        assertThat(new ValidationException("test")).isInstanceOf(RuntimeException.class);
        assertThat(new ConflictException("test")).isInstanceOf(RuntimeException.class);
        assertThat(new UnauthorizedException("test")).isInstanceOf(RuntimeException.class);
        assertThat(new ForbiddenException("test")).isInstanceOf(RuntimeException.class);
        assertThat(new ServiceUnavailableException("test")).isInstanceOf(RuntimeException.class);
        assertThat(new UpstreamServiceException("test")).isInstanceOf(RuntimeException.class);
    }
}
