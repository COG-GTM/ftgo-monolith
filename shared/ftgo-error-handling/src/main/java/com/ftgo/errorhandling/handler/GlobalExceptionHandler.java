package com.ftgo.errorhandling.handler;

import com.ftgo.errorhandling.constants.ErrorCodes;
import com.ftgo.errorhandling.dto.ErrorResponse;
import com.ftgo.errorhandling.exception.BusinessRuleException;
import com.ftgo.errorhandling.exception.ConflictException;
import com.ftgo.errorhandling.exception.ResourceNotFoundException;
import com.ftgo.errorhandling.exception.ServiceCommunicationException;
import net.chrisrichardson.ftgo.common.UnsupportedStateTransitionException;
import net.chrisrichardson.ftgo.domain.OrderMinimumNotMetException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import java.net.ConnectException;
import java.util.stream.Collectors;

/**
 * Global exception handler for all FTGO microservices.
 *
 * <p>This {@link RestControllerAdvice} provides centralized exception handling that
 * converts all exceptions into a standardized {@link ErrorResponse} format. It ensures:</p>
 * <ul>
 *   <li>Consistent error response format across all services</li>
 *   <li>Proper HTTP status code mapping for each exception type</li>
 *   <li>TraceId inclusion from distributed tracing context</li>
 *   <li>No raw stack traces leaked to clients</li>
 *   <li>Structured logging of all errors for observability</li>
 * </ul>
 *
 * <h3>Exception Handling Hierarchy:</h3>
 * <table>
 *   <tr><th>Exception</th><th>HTTP Status</th><th>Error Code</th></tr>
 *   <tr><td>MethodArgumentNotValidException</td><td>400</td><td>FTGO-01001</td></tr>
 *   <tr><td>BindException</td><td>400</td><td>FTGO-01001</td></tr>
 *   <tr><td>HttpMessageNotReadableException</td><td>400</td><td>FTGO-00005</td></tr>
 *   <tr><td>MissingServletRequestParameterException</td><td>400</td><td>FTGO-00006</td></tr>
 *   <tr><td>MethodArgumentTypeMismatchException</td><td>400</td><td>FTGO-00007</td></tr>
 *   <tr><td>ResourceNotFoundException</td><td>404</td><td>FTGO-00002</td></tr>
 *   <tr><td>NoHandlerFoundException</td><td>404</td><td>FTGO-00002</td></tr>
 *   <tr><td>HttpRequestMethodNotSupportedException</td><td>405</td><td>FTGO-00003</td></tr>
 *   <tr><td>UnsupportedStateTransitionException</td><td>409</td><td>FTGO-03002</td></tr>
 *   <tr><td>ConflictException</td><td>409</td><td>varies</td></tr>
 *   <tr><td>HttpMediaTypeNotSupportedException</td><td>415</td><td>FTGO-00004</td></tr>
 *   <tr><td>OrderMinimumNotMetException</td><td>422</td><td>FTGO-03003</td></tr>
 *   <tr><td>BusinessRuleException</td><td>422</td><td>varies</td></tr>
 *   <tr><td>ServiceCommunicationException</td><td>502/503</td><td>varies</td></tr>
 *   <tr><td>ConnectException</td><td>503</td><td>FTGO-07005</td></tr>
 *   <tr><td>Exception (fallback)</td><td>500</td><td>FTGO-00001</td></tr>
 * </table>
 *
 * <p>Services include this library as a dependency to automatically enable
 * centralized error handling via Spring Boot auto-configuration.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** MDC key for trace ID (compatible with ftgo-tracing library). */
    private static final String MDC_TRACE_ID = "traceId";

    // -------------------------------------------------------------------------
    // Validation Errors (400 Bad Request)
    // -------------------------------------------------------------------------

    /**
     * Handles Bean Validation failures on @RequestBody parameters annotated with @Valid.
     * Returns 400 with detailed field-level error information.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        ErrorResponse response = buildValidationErrorResponse(ex.getBindingResult(), request);
        log.warn("Validation failed for {} {}: {} field error(s)",
                request.getMethod(), request.getRequestURI(),
                ex.getBindingResult().getFieldErrorCount());

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles Bean Validation failures on @ModelAttribute or form data.
     * Returns 400 with detailed field-level error information.
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(
            BindException ex, HttpServletRequest request) {

        ErrorResponse response = buildValidationErrorResponse(ex.getBindingResult(), request);
        log.warn("Binding failed for {} {}: {} field error(s)",
                request.getMethod(), request.getRequestURI(),
                ex.getBindingResult().getFieldErrorCount());

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles malformed or unreadable request bodies (e.g., invalid JSON).
     * Returns 400 with a description of the parse error.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {

        String details = "Request body is missing or contains invalid JSON";
        ErrorResponse response = buildErrorResponse(
                ErrorCodes.MALFORMED_REQUEST, "Malformed request body", details,
                HttpStatus.BAD_REQUEST, request);

        log.warn("Malformed request body for {} {}: {}",
                request.getMethod(), request.getRequestURI(), ex.getMessage());

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles missing required request parameters.
     * Returns 400 with the name of the missing parameter.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex, HttpServletRequest request) {

        String details = String.format("Required parameter '%s' of type '%s' is missing",
                ex.getParameterName(), ex.getParameterType());
        ErrorResponse response = buildErrorResponse(
                ErrorCodes.MISSING_PARAMETER, "Missing request parameter", details,
                HttpStatus.BAD_REQUEST, request);

        log.warn("Missing parameter '{}' for {} {}",
                ex.getParameterName(), request.getMethod(), request.getRequestURI());

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles type mismatch in request parameters (e.g., string where number expected).
     * Returns 400 with details about the expected type.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

        String requiredType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown";
        String details = String.format("Parameter '%s' should be of type '%s' but received '%s'",
                ex.getName(), requiredType, ex.getValue());
        ErrorResponse response = buildErrorResponse(
                ErrorCodes.TYPE_MISMATCH, "Type mismatch in request parameter", details,
                HttpStatus.BAD_REQUEST, request);

        log.warn("Type mismatch for parameter '{}' in {} {}: expected {} but got '{}'",
                ex.getName(), request.getMethod(), request.getRequestURI(),
                requiredType, ex.getValue());

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // -------------------------------------------------------------------------
    // Not Found Errors (404)
    // -------------------------------------------------------------------------

    /**
     * Handles resource not found exceptions.
     * Returns 404 with resource type and identifier when available.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {

        ErrorResponse response = buildErrorResponse(
                ErrorCodes.RESOURCE_NOT_FOUND, "Resource not found", ex.getMessage(),
                HttpStatus.NOT_FOUND, request);

        log.warn("Resource not found: {} for {} {}",
                ex.getMessage(), request.getMethod(), request.getRequestURI());

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles requests to endpoints that do not exist.
     * Returns 404 with the requested path.
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFound(
            NoHandlerFoundException ex, HttpServletRequest request) {

        String details = String.format("No handler found for %s %s", ex.getHttpMethod(), ex.getRequestURL());
        ErrorResponse response = buildErrorResponse(
                ErrorCodes.RESOURCE_NOT_FOUND, "Endpoint not found", details,
                HttpStatus.NOT_FOUND, request);

        log.warn("No handler found for {} {}", ex.getHttpMethod(), ex.getRequestURL());

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    // -------------------------------------------------------------------------
    // Method Not Allowed (405)
    // -------------------------------------------------------------------------

    /**
     * Handles unsupported HTTP methods on valid endpoints.
     * Returns 405 with the list of supported methods.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {

        String supported = ex.getSupportedHttpMethods() != null
                ? ex.getSupportedHttpMethods().stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(", "))
                : "none";
        String details = String.format("Method '%s' is not supported. Supported methods: %s",
                ex.getMethod(), supported);
        ErrorResponse response = buildErrorResponse(
                ErrorCodes.METHOD_NOT_ALLOWED, "Method not allowed", details,
                HttpStatus.METHOD_NOT_ALLOWED, request);

        log.warn("Method '{}' not allowed for {}, supported: {}",
                ex.getMethod(), request.getRequestURI(), supported);

        return new ResponseEntity<>(response, HttpStatus.METHOD_NOT_ALLOWED);
    }

    // -------------------------------------------------------------------------
    // Conflict Errors (409)
    // -------------------------------------------------------------------------

    /**
     * Handles unsupported state transition exceptions from the FTGO domain.
     * Maps to 409 Conflict as the operation conflicts with the current entity state.
     */
    @ExceptionHandler(UnsupportedStateTransitionException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedStateTransition(
            UnsupportedStateTransitionException ex, HttpServletRequest request) {

        ErrorResponse response = buildErrorResponse(
                ErrorCodes.ORDER_INVALID_STATE_TRANSITION,
                "Invalid state transition",
                ex.getMessage(),
                HttpStatus.CONFLICT, request);

        log.warn("Unsupported state transition for {} {}: {}",
                request.getMethod(), request.getRequestURI(), ex.getMessage());

        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    /**
     * Handles generic conflict exceptions.
     * Returns 409 with the specific error code from the exception.
     */
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(
            ConflictException ex, HttpServletRequest request) {

        ErrorResponse response = buildErrorResponse(
                ex.getErrorCode(), "Conflict", ex.getMessage(),
                HttpStatus.CONFLICT, request);

        log.warn("Conflict for {} {}: [{}] {}",
                request.getMethod(), request.getRequestURI(),
                ex.getErrorCode(), ex.getMessage());

        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    // -------------------------------------------------------------------------
    // Unsupported Media Type (415)
    // -------------------------------------------------------------------------

    /**
     * Handles unsupported content types in requests.
     * Returns 415 with the list of supported media types.
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {

        String supported = ex.getSupportedMediaTypes().stream()
                .map(Object::toString)
                .collect(Collectors.joining(", "));
        String details = String.format("Content type '%s' is not supported. Supported types: %s",
                ex.getContentType(), supported);
        ErrorResponse response = buildErrorResponse(
                ErrorCodes.UNSUPPORTED_MEDIA_TYPE, "Unsupported media type", details,
                HttpStatus.UNSUPPORTED_MEDIA_TYPE, request);

        log.warn("Unsupported media type '{}' for {} {}, supported: {}",
                ex.getContentType(), request.getMethod(), request.getRequestURI(), supported);

        return new ResponseEntity<>(response, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    // -------------------------------------------------------------------------
    // Business Rule Violations (422 Unprocessable Entity)
    // -------------------------------------------------------------------------

    /**
     * Handles order minimum not met exceptions from the FTGO domain.
     * Maps to 422 Unprocessable Entity as the request is syntactically valid
     * but violates a business rule.
     */
    @ExceptionHandler(OrderMinimumNotMetException.class)
    public ResponseEntity<ErrorResponse> handleOrderMinimumNotMet(
            OrderMinimumNotMetException ex, HttpServletRequest request) {

        ErrorResponse response = buildErrorResponse(
                ErrorCodes.ORDER_MINIMUM_NOT_MET,
                "Order minimum not met",
                "The order total does not meet the restaurant's minimum order requirement",
                HttpStatus.UNPROCESSABLE_ENTITY, request);

        log.warn("Order minimum not met for {} {}", request.getMethod(), request.getRequestURI());

        return new ResponseEntity<>(response, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    /**
     * Handles generic business rule exceptions.
     * Returns 422 with the specific error code from the exception.
     */
    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ErrorResponse> handleBusinessRule(
            BusinessRuleException ex, HttpServletRequest request) {

        ErrorResponse response = buildErrorResponse(
                ex.getErrorCode(), "Business rule violation", ex.getMessage(),
                HttpStatus.UNPROCESSABLE_ENTITY, request);

        log.warn("Business rule violation for {} {}: [{}] {}",
                request.getMethod(), request.getRequestURI(),
                ex.getErrorCode(), ex.getMessage());

        return new ResponseEntity<>(response, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    // -------------------------------------------------------------------------
    // Inter-Service Communication Errors (502/503)
    // -------------------------------------------------------------------------

    /**
     * Handles inter-service communication failures.
     * Returns 502 Bad Gateway for downstream errors, 503 Service Unavailable for
     * timeout/connection failures.
     */
    @ExceptionHandler(ServiceCommunicationException.class)
    public ResponseEntity<ErrorResponse> handleServiceCommunication(
            ServiceCommunicationException ex, HttpServletRequest request) {

        HttpStatus status = resolveServiceCommunicationStatus(ex.getErrorCode());
        ErrorResponse response = buildErrorResponse(
                ex.getErrorCode(),
                "Service communication failure",
                String.format("Failed to communicate with %s: %s",
                        ex.getTargetService(), ex.getMessage()),
                status, request);

        log.error("Service communication failure to '{}' for {} {}: [{}] {}",
                ex.getTargetService(), request.getMethod(), request.getRequestURI(),
                ex.getErrorCode(), ex.getMessage(), ex);

        return new ResponseEntity<>(response, status);
    }

    /**
     * Handles connection exceptions (typically from RestTemplate or WebClient).
     * Returns 503 Service Unavailable.
     */
    @ExceptionHandler(ConnectException.class)
    public ResponseEntity<ErrorResponse> handleConnectException(
            ConnectException ex, HttpServletRequest request) {

        ErrorResponse response = buildErrorResponse(
                ErrorCodes.SERVICE_CONNECTION_FAILURE,
                "Service connection failure",
                "Unable to establish connection to downstream service",
                HttpStatus.SERVICE_UNAVAILABLE, request);

        log.error("Connection failure for {} {}: {}",
                request.getMethod(), request.getRequestURI(), ex.getMessage(), ex);

        return new ResponseEntity<>(response, HttpStatus.SERVICE_UNAVAILABLE);
    }

    // -------------------------------------------------------------------------
    // Fallback Handler (500 Internal Server Error)
    // -------------------------------------------------------------------------

    /**
     * Catches all unhandled exceptions as a safety net.
     * Returns 500 with a generic message - never exposes stack traces to clients.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllUncaughtExceptions(
            Exception ex, HttpServletRequest request) {

        ErrorResponse response = buildErrorResponse(
                ErrorCodes.INTERNAL_ERROR,
                "Internal server error",
                "An unexpected error occurred. Please try again later.",
                HttpStatus.INTERNAL_SERVER_ERROR, request);

        log.error("Unhandled exception for {} {}: {}",
                request.getMethod(), request.getRequestURI(), ex.getMessage(), ex);

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // -------------------------------------------------------------------------
    // Helper Methods
    // -------------------------------------------------------------------------

    /**
     * Builds a standardized {@link ErrorResponse} with trace ID and request path.
     */
    private ErrorResponse buildErrorResponse(String code, String message, String details,
                                              HttpStatus status, HttpServletRequest request) {
        ErrorResponse response = new ErrorResponse(code, message, details, status.value());
        response.setTraceId(getTraceId());
        response.setPath(request.getRequestURI());
        return response;
    }

    /**
     * Builds a validation-specific {@link ErrorResponse} with field-level errors.
     */
    private ErrorResponse buildValidationErrorResponse(BindingResult bindingResult,
                                                        HttpServletRequest request) {
        ErrorResponse response = new ErrorResponse(
                ErrorCodes.VALIDATION_FAILED,
                "Validation failed",
                String.format("%d field(s) have validation errors",
                        bindingResult.getFieldErrorCount()),
                HttpStatus.BAD_REQUEST.value());
        response.setTraceId(getTraceId());
        response.setPath(request.getRequestURI());

        bindingResult.getFieldErrors().forEach(fieldError ->
                response.addFieldError(
                        fieldError.getField(),
                        fieldError.getDefaultMessage(),
                        fieldError.getRejectedValue()));

        return response;
    }

    /**
     * Retrieves the current trace ID from the SLF4J MDC context.
     * The trace ID is populated by the ftgo-tracing library's Brave/Micrometer integration.
     *
     * @return the current trace ID, or "N/A" if not available
     */
    private String getTraceId() {
        String traceId = MDC.get(MDC_TRACE_ID);
        return traceId != null ? traceId : "N/A";
    }

    /**
     * Resolves the appropriate HTTP status code for service communication errors.
     */
    private HttpStatus resolveServiceCommunicationStatus(String errorCode) {
        if (ErrorCodes.SERVICE_TIMEOUT.equals(errorCode)
                || ErrorCodes.SERVICE_UNAVAILABLE.equals(errorCode)
                || ErrorCodes.CIRCUIT_BREAKER_OPEN.equals(errorCode)
                || ErrorCodes.SERVICE_CONNECTION_FAILURE.equals(errorCode)) {
            return HttpStatus.SERVICE_UNAVAILABLE;
        }
        return HttpStatus.BAD_GATEWAY;
    }
}
