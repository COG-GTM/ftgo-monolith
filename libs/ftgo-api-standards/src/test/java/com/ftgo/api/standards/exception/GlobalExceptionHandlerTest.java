package com.ftgo.api.standards.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.ftgo.api.standards.dto.ErrorResponse;
import com.ftgo.api.standards.filter.CorrelationIdFilter;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/orders");
        MDC.put(CorrelationIdFilter.CORRELATION_ID_MDC_KEY, "test-correlation-id");
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void handleBusinessException() {
        BusinessException ex = new BusinessException("Order cannot be cancelled");

        ResponseEntity<ErrorResponse> response = handler.handleBaseException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getStatus()).isEqualTo(422);
        assertThat(body.getCode()).isEqualTo("UNPROCESSABLE_ENTITY");
        assertThat(body.getMessage()).isEqualTo("Order cannot be cancelled");
        assertThat(body.getPath()).isEqualTo("/api/v1/orders");
        assertThat(body.getCorrelationId()).isEqualTo("test-correlation-id");
        assertThat(body.getTimestamp()).isNotNull();
    }

    @Test
    void handleResourceNotFoundException() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Order", 42L);

        ResponseEntity<ErrorResponse> response = handler.handleBaseException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getStatus()).isEqualTo(404);
        assertThat(body.getCode()).isEqualTo("NOT_FOUND");
        assertThat(body.getMessage()).isEqualTo("Order not found with id: 42");
        assertThat(body.getCorrelationId()).isEqualTo("test-correlation-id");
    }

    @Test
    void handleValidationException() {
        Map<String, String> fieldErrors = Map.of("email", "must be valid", "name", "must not be blank");
        ValidationException ex = new ValidationException("Validation failed", fieldErrors);

        ResponseEntity<ErrorResponse> response = handler.handleValidationException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getStatus()).isEqualTo(400);
        assertThat(body.getCode()).isEqualTo("VALIDATION_ERROR");
        assertThat(body.getErrors()).hasSize(2);
        assertThat(body.getCorrelationId()).isEqualTo("test-correlation-id");
    }

    @Test
    void handleConflictException() {
        ConflictException ex = new ConflictException("Resource already exists");

        ResponseEntity<ErrorResponse> response = handler.handleBaseException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getStatus()).isEqualTo(409);
        assertThat(body.getCode()).isEqualTo("CONFLICT");
    }

    @Test
    void handleUnauthorizedException() {
        UnauthorizedException ex = new UnauthorizedException("Invalid token");

        ResponseEntity<ErrorResponse> response = handler.handleBaseException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getStatus()).isEqualTo(401);
        assertThat(body.getCode()).isEqualTo("UNAUTHORIZED");
    }

    @Test
    void handleForbiddenException() {
        ForbiddenException ex = new ForbiddenException("Insufficient permissions");

        ResponseEntity<ErrorResponse> response = handler.handleBaseException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getStatus()).isEqualTo(403);
        assertThat(body.getCode()).isEqualTo("FORBIDDEN");
    }

    @Test
    void handleServiceUnavailableException() {
        ServiceUnavailableException ex = new ServiceUnavailableException("Service is down");

        ResponseEntity<ErrorResponse> response = handler.handleBaseException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getStatus()).isEqualTo(503);
        assertThat(body.getCode()).isEqualTo("SERVICE_UNAVAILABLE");
    }

    @Test
    void handleUpstreamServiceException() {
        UpstreamServiceException ex = new UpstreamServiceException("Payment service failed");

        ResponseEntity<ErrorResponse> response = handler.handleBaseException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getStatus()).isEqualTo(502);
        assertThat(body.getCode()).isEqualTo("UPSTREAM_ERROR");
    }

    @Test
    void handleGenericException() {
        Exception ex = new RuntimeException("Something went wrong");

        ResponseEntity<ErrorResponse> response = handler.handleGenericException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getStatus()).isEqualTo(500);
        assertThat(body.getCode()).isEqualTo("INTERNAL_ERROR");
        assertThat(body.getMessage()).isEqualTo("An unexpected error occurred");
        assertThat(body.getCorrelationId()).isEqualTo("test-correlation-id");
    }

    @Test
    void handleMissingParameter() {
        org.springframework.web.bind.MissingServletRequestParameterException ex =
                new org.springframework.web.bind.MissingServletRequestParameterException("orderId", "String");

        ResponseEntity<ErrorResponse> response = handler.handleMissingParameter(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getCode()).isEqualTo("BAD_REQUEST");
    }

    @Test
    void handleMessageNotReadable() {
        org.springframework.http.converter.HttpMessageNotReadableException ex =
                new org.springframework.http.converter.HttpMessageNotReadableException("Malformed JSON");

        ResponseEntity<ErrorResponse> response = handler.handleMessageNotReadable(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getCode()).isEqualTo("BAD_REQUEST");
        assertThat(body.getMessage()).isEqualTo("Malformed request body");
    }

    @Test
    void handleMethodNotAllowed() {
        org.springframework.web.HttpRequestMethodNotSupportedException ex =
                new org.springframework.web.HttpRequestMethodNotSupportedException("DELETE");

        ResponseEntity<ErrorResponse> response = handler.handleMethodNotAllowed(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
        ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getCode()).isEqualTo("METHOD_NOT_ALLOWED");
    }

    @Test
    void correlationIdIncludedWhenPresent() {
        BusinessException ex = new BusinessException("test");

        ResponseEntity<ErrorResponse> response = handler.handleBaseException(ex, request);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCorrelationId()).isEqualTo("test-correlation-id");
    }

    @Test
    void correlationIdNullWhenNotInMdc() {
        MDC.clear();
        BusinessException ex = new BusinessException("test");

        ResponseEntity<ErrorResponse> response = handler.handleBaseException(ex, request);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCorrelationId()).isNull();
    }
}
