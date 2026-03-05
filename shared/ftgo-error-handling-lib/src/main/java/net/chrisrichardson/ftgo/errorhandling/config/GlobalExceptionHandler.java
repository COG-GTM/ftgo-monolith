package net.chrisrichardson.ftgo.errorhandling.config;

import io.micrometer.tracing.Tracer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import net.chrisrichardson.ftgo.common.NotYetImplementedException;
import net.chrisrichardson.ftgo.common.UnsupportedStateTransitionException;
import net.chrisrichardson.ftgo.domain.OrderMinimumNotMetException;
import net.chrisrichardson.ftgo.errorhandling.constants.FtgoErrorCodes;
import net.chrisrichardson.ftgo.errorhandling.exception.ResourceNotFoundException;
import net.chrisrichardson.ftgo.errorhandling.exception.ServiceCommunicationException;
import net.chrisrichardson.ftgo.errorhandling.model.FtgoErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Centralized exception handler for all FTGO microservices.
 *
 * <p>This {@link RestControllerAdvice} catches exceptions thrown by REST controllers
 * and converts them into standardized {@link FtgoErrorResponse} envelopes with
 * appropriate HTTP status codes. Every response includes:</p>
 * <ul>
 *     <li>A consistent error code from {@link FtgoErrorCodes}</li>
 *     <li>A human-readable message (no stack traces leaked to clients)</li>
 *     <li>The distributed traceId for log correlation</li>
 *     <li>An ISO 8601 timestamp</li>
 * </ul>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final Tracer tracer;

    public GlobalExceptionHandler(Tracer tracer) {
        this.tracer = tracer;
    }

    // =========================================================================
    // Domain / Business Exceptions
    // =========================================================================

    /**
     * Handles invalid state transitions (e.g., cancelling an already delivered order).
     *
     * @return 409 Conflict
     */
    @ExceptionHandler(UnsupportedStateTransitionException.class)
    public ResponseEntity<FtgoErrorResponse> handleUnsupportedStateTransition(
            UnsupportedStateTransitionException ex, HttpServletRequest request) {
        log.warn("Unsupported state transition: {}", ex.getMessage());
        FtgoErrorResponse response = FtgoErrorResponse.of(
                FtgoErrorCodes.UNSUPPORTED_STATE_TRANSITION,
                ex.getMessage(),
                request.getRequestURI(),
                getCurrentTraceId());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    /**
     * Handles order minimum not met violations.
     *
     * @return 422 Unprocessable Entity
     */
    @ExceptionHandler(OrderMinimumNotMetException.class)
    public ResponseEntity<FtgoErrorResponse> handleOrderMinimumNotMet(
            OrderMinimumNotMetException ex, HttpServletRequest request) {
        log.warn("Order minimum not met: {}", ex.getMessage());
        FtgoErrorResponse response = FtgoErrorResponse.of(
                FtgoErrorCodes.ORDER_MINIMUM_NOT_MET,
                "Order total does not meet the restaurant minimum",
                request.getRequestURI(),
                getCurrentTraceId());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
    }

    /**
     * Handles not-yet-implemented functionality.
     *
     * @return 501 Not Implemented
     */
    @ExceptionHandler(NotYetImplementedException.class)
    public ResponseEntity<FtgoErrorResponse> handleNotYetImplemented(
            NotYetImplementedException ex, HttpServletRequest request) {
        log.info("Not yet implemented endpoint hit: {}", request.getRequestURI());
        FtgoErrorResponse response = FtgoErrorResponse.of(
                FtgoErrorCodes.NOT_IMPLEMENTED,
                "This functionality is not yet implemented",
                request.getRequestURI(),
                getCurrentTraceId());
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(response);
    }

    /**
     * Handles resource not found exceptions.
     *
     * @return 404 Not Found
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<FtgoErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {
        log.warn("Resource not found: {}", ex.getMessage());
        FtgoErrorResponse response = FtgoErrorResponse.of(
                FtgoErrorCodes.RESOURCE_NOT_FOUND,
                ex.getMessage(),
                request.getRequestURI(),
                getCurrentTraceId());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    // =========================================================================
    // Bean Validation Exceptions
    // =========================================================================

    /**
     * Handles Bean Validation failures on @Valid @RequestBody DTOs.
     *
     * @return 400 Bad Request with field-level details
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<FtgoErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.warn("Validation failed for request to {}: {} error(s)",
                request.getRequestURI(), ex.getBindingResult().getErrorCount());

        List<FtgoErrorResponse.FieldError> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new FtgoErrorResponse.FieldError(
                        error.getField(),
                        error.getRejectedValue(),
                        error.getDefaultMessage()))
                .collect(Collectors.toList());

        FtgoErrorResponse response = FtgoErrorResponse.of(
                FtgoErrorCodes.VALIDATION_FAILED,
                "Validation failed",
                fieldErrors,
                request.getRequestURI(),
                getCurrentTraceId());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handles constraint violations on method parameters (@Validated on controller).
     *
     * @return 400 Bad Request with constraint details
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<FtgoErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {
        log.warn("Constraint violation for request to {}: {}", request.getRequestURI(), ex.getMessage());

        List<FtgoErrorResponse.FieldError> fieldErrors = ex.getConstraintViolations()
                .stream()
                .map(this::toFieldError)
                .collect(Collectors.toList());

        FtgoErrorResponse response = FtgoErrorResponse.of(
                FtgoErrorCodes.CONSTRAINT_VIOLATION,
                "Constraint violation",
                fieldErrors,
                request.getRequestURI(),
                getCurrentTraceId());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // =========================================================================
    // HTTP / Spring MVC Exceptions
    // =========================================================================

    /**
     * Handles malformed request bodies (unparseable JSON, etc.).
     *
     * @return 400 Bad Request
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<FtgoErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.warn("Message not readable for request to {}: {}", request.getRequestURI(), ex.getMessage());
        FtgoErrorResponse response = FtgoErrorResponse.of(
                FtgoErrorCodes.BAD_REQUEST,
                "Malformed request body",
                request.getRequestURI(),
                getCurrentTraceId());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handles requests to non-existent handler mappings.
     *
     * @return 404 Not Found
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<FtgoErrorResponse> handleNoHandlerFound(
            NoHandlerFoundException ex, HttpServletRequest request) {
        log.warn("No handler found: {} {}", ex.getHttpMethod(), ex.getRequestURL());
        FtgoErrorResponse response = FtgoErrorResponse.of(
                FtgoErrorCodes.RESOURCE_NOT_FOUND,
                "No handler found for " + ex.getHttpMethod() + " " + ex.getRequestURL(),
                request.getRequestURI(),
                getCurrentTraceId());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Handles requests to non-existent static resources (Spring Boot 3.2+).
     *
     * @return 404 Not Found
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<FtgoErrorResponse> handleNoResourceFound(
            NoResourceFoundException ex, HttpServletRequest request) {
        log.warn("No resource found: {}", request.getRequestURI());
        FtgoErrorResponse response = FtgoErrorResponse.of(
                FtgoErrorCodes.RESOURCE_NOT_FOUND,
                "Resource not found",
                request.getRequestURI(),
                getCurrentTraceId());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Handles unsupported HTTP methods (e.g., POST to a GET-only endpoint).
     *
     * @return 405 Method Not Allowed
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<FtgoErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        log.warn("Method not supported: {} for {}", ex.getMethod(), request.getRequestURI());
        FtgoErrorResponse response = FtgoErrorResponse.of(
                FtgoErrorCodes.METHOD_NOT_ALLOWED,
                "HTTP method '" + ex.getMethod() + "' is not supported for this endpoint",
                request.getRequestURI(),
                getCurrentTraceId());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }

    // =========================================================================
    // Security Exceptions
    // =========================================================================

    /**
     * Handles access denied (insufficient permissions).
     *
     * @return 403 Forbidden
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<FtgoErrorResponse> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Access denied for request to {}: {}", request.getRequestURI(), ex.getMessage());
        FtgoErrorResponse response = FtgoErrorResponse.of(
                FtgoErrorCodes.ACCESS_DENIED,
                "Access denied",
                request.getRequestURI(),
                getCurrentTraceId());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    /**
     * Handles authentication failures.
     *
     * @return 401 Unauthorized
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<FtgoErrorResponse> handleAuthenticationException(
            AuthenticationException ex, HttpServletRequest request) {
        log.warn("Authentication failed for request to {}: {}", request.getRequestURI(), ex.getMessage());
        FtgoErrorResponse response = FtgoErrorResponse.of(
                FtgoErrorCodes.UNAUTHORIZED,
                "Authentication required",
                request.getRequestURI(),
                getCurrentTraceId());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    // =========================================================================
    // Inter-Service Communication Exceptions
    // =========================================================================

    /**
     * Handles custom inter-service communication failures.
     *
     * @return 502 Bad Gateway or 504 Gateway Timeout
     */
    @ExceptionHandler(ServiceCommunicationException.class)
    public ResponseEntity<FtgoErrorResponse> handleServiceCommunication(
            ServiceCommunicationException ex, HttpServletRequest request) {
        log.error("Service communication failure with '{}': {}", ex.getServiceName(), ex.getMessage(), ex);

        if (ex.isTimeout()) {
            FtgoErrorResponse response = FtgoErrorResponse.of(
                    FtgoErrorCodes.SERVICE_TIMEOUT,
                    "Downstream service '" + ex.getServiceName() + "' did not respond in time",
                    request.getRequestURI(),
                    getCurrentTraceId());
            return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(response);
        }

        FtgoErrorResponse response = FtgoErrorResponse.of(
                FtgoErrorCodes.SERVICE_COMMUNICATION_ERROR,
                "Error communicating with downstream service '" + ex.getServiceName() + "'",
                request.getRequestURI(),
                getCurrentTraceId());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(response);
    }

    /**
     * Handles Spring RestTemplate/WebClient connection failures.
     *
     * @return 503 Service Unavailable
     */
    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<FtgoErrorResponse> handleResourceAccess(
            ResourceAccessException ex, HttpServletRequest request) {
        log.error("Resource access failure for request to {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        FtgoErrorResponse response = FtgoErrorResponse.of(
                FtgoErrorCodes.SERVICE_UNAVAILABLE,
                "A downstream service is currently unavailable",
                request.getRequestURI(),
                getCurrentTraceId());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    /**
     * Handles general Spring REST client errors.
     *
     * @return 502 Bad Gateway
     */
    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<FtgoErrorResponse> handleRestClient(
            RestClientException ex, HttpServletRequest request) {
        log.error("REST client error for request to {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        FtgoErrorResponse response = FtgoErrorResponse.of(
                FtgoErrorCodes.SERVICE_COMMUNICATION_ERROR,
                "Error communicating with downstream service",
                request.getRequestURI(),
                getCurrentTraceId());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(response);
    }

    // =========================================================================
    // Catch-All
    // =========================================================================

    /**
     * Handles all unhandled exceptions. Never leaks stack traces to the client.
     *
     * @return 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<FtgoErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception for request to {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        FtgoErrorResponse response = FtgoErrorResponse.of(
                FtgoErrorCodes.INTERNAL_ERROR,
                "An unexpected error occurred",
                request.getRequestURI(),
                getCurrentTraceId());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    /**
     * Extracts the current trace ID from the Micrometer Tracer.
     *
     * @return the current trace ID, or null if no active span
     */
    private String getCurrentTraceId() {
        if (tracer != null && tracer.currentSpan() != null && tracer.currentSpan().context() != null) {
            return tracer.currentSpan().context().traceId();
        }
        return null;
    }

    /**
     * Converts a {@link ConstraintViolation} to a {@link FtgoErrorResponse.FieldError}.
     */
    private FtgoErrorResponse.FieldError toFieldError(ConstraintViolation<?> violation) {
        String propertyPath = violation.getPropertyPath().toString();
        // Extract just the field name (last segment of the property path)
        int lastDot = propertyPath.lastIndexOf('.');
        String fieldName = lastDot >= 0 ? propertyPath.substring(lastDot + 1) : propertyPath;
        return new FtgoErrorResponse.FieldError(
                fieldName,
                violation.getInvalidValue(),
                violation.getMessage());
    }
}
