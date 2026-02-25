package com.ftgo.common.error.handler;

import com.ftgo.common.error.code.CommonErrorCode;
import com.ftgo.common.error.exception.FtgoException;
import com.ftgo.common.error.response.ErrorDetail;
import com.ftgo.common.error.response.ErrorResponse;
import com.ftgo.common.error.response.ErrorResponseWrapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
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
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Global exception handler for all FTGO microservices.
 *
 * <p>Provides centralized exception handling using {@code @RestControllerAdvice},
 * mapping all exceptions to the standardized {@link ErrorResponse} format.</p>
 *
 * <h3>Exception Mapping</h3>
 * <ul>
 *   <li>{@link FtgoException} subclasses &rarr; HTTP status from {@code ErrorCode}</li>
 *   <li>{@link MethodArgumentNotValidException} &rarr; 400 Bad Request</li>
 *   <li>{@link ConstraintViolationException} &rarr; 400 Bad Request</li>
 *   <li>{@link HttpMessageNotReadableException} &rarr; 400 Bad Request</li>
 *   <li>{@link NoHandlerFoundException} &rarr; 404 Not Found</li>
 *   <li>{@link HttpRequestMethodNotSupportedException} &rarr; 405 Method Not Allowed</li>
 *   <li>All other exceptions &rarr; 500 Internal Server Error</li>
 * </ul>
 *
 * <p>Services can extend this class to add domain-specific exception handlers.</p>
 *
 * @see ErrorResponse
 * @see FtgoException
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final TraceIdProvider traceIdProvider;

    public GlobalExceptionHandler(TraceIdProvider traceIdProvider) {
        this.traceIdProvider = traceIdProvider;
    }

    // =========================================================================
    // FTGO Custom Exceptions
    // =========================================================================

    /**
     * Handles all {@link FtgoException} subclasses.
     * The HTTP status code is derived from the exception's {@link com.ftgo.common.error.code.ErrorCode}.
     */
    @ExceptionHandler(FtgoException.class)
    public ResponseEntity<ErrorResponseWrapper> handleFtgoException(
            FtgoException ex, WebRequest request) {
        log.warn("FTGO exception [{}]: {}", ex.getErrorCode().getCode(), ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .code(ex.getErrorCode().getCode())
                .message(ex.getMessage())
                .status(ex.getHttpStatus())
                .timestamp(Instant.now())
                .traceId(traceIdProvider.getTraceId())
                .instance(getRequestPath(request))
                .build();

        return ResponseEntity
                .status(ex.getHttpStatus())
                .body(ErrorResponseWrapper.of(error));
    }

    // =========================================================================
    // Bean Validation Exceptions
    // =========================================================================

    /**
     * Handles {@link ConstraintViolationException} from Bean Validation
     * on path variables and request parameters.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseWrapper> handleConstraintViolation(
            ConstraintViolationException ex, WebRequest request) {
        log.warn("Constraint violation: {}", ex.getMessage());

        List<ErrorDetail> details = ex.getConstraintViolations().stream()
                .map(this::toErrorDetail)
                .collect(Collectors.toList());

        ErrorResponse error = ErrorResponse.builder()
                .code(CommonErrorCode.VALIDATION_ERROR.getCode())
                .message(CommonErrorCode.VALIDATION_ERROR.getDefaultMessage())
                .status(HttpStatus.BAD_REQUEST.value())
                .details(details)
                .timestamp(Instant.now())
                .traceId(traceIdProvider.getTraceId())
                .instance(getRequestPath(request))
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponseWrapper.of(error));
    }

    /**
     * Handles {@link MethodArgumentTypeMismatchException} for type conversion errors.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseWrapper> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex, WebRequest request) {
        log.warn("Argument type mismatch: {}", ex.getMessage());

        String expectedType = ex.getRequiredType() != null
                ? ex.getRequiredType().getSimpleName()
                : "unknown";

        String message = String.format("Parameter '%s' should be of type '%s'",
                ex.getName(), expectedType);

        ErrorResponse error = ErrorResponse.builder()
                .code(CommonErrorCode.INVALID_REQUEST.getCode())
                .message(message)
                .status(HttpStatus.BAD_REQUEST.value())
                .timestamp(Instant.now())
                .traceId(traceIdProvider.getTraceId())
                .instance(getRequestPath(request))
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponseWrapper.of(error));
    }

    // =========================================================================
    // Spring MVC Exceptions (overrides from ResponseEntityExceptionHandler)
    // =========================================================================

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        log.warn("Validation failed: {}", ex.getMessage());

        List<ErrorDetail> details = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> ErrorDetail.of(
                        fieldError.getField(),
                        fieldError.getDefaultMessage(),
                        fieldError.getRejectedValue()))
                .collect(Collectors.toList());

        ErrorResponse error = ErrorResponse.builder()
                .code(CommonErrorCode.VALIDATION_ERROR.getCode())
                .message(CommonErrorCode.VALIDATION_ERROR.getDefaultMessage())
                .status(HttpStatus.BAD_REQUEST.value())
                .details(details)
                .timestamp(Instant.now())
                .traceId(traceIdProvider.getTraceId())
                .instance(getRequestPath(request))
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponseWrapper.of(error));
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        log.warn("Malformed request body: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .code(CommonErrorCode.INVALID_REQUEST.getCode())
                .message("Malformed JSON request body")
                .status(HttpStatus.BAD_REQUEST.value())
                .timestamp(Instant.now())
                .traceId(traceIdProvider.getTraceId())
                .instance(getRequestPath(request))
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponseWrapper.of(error));
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        log.warn("Missing request parameter: {}", ex.getMessage());

        String message = String.format("Required parameter '%s' of type '%s' is missing",
                ex.getParameterName(), ex.getParameterType());

        ErrorResponse error = ErrorResponse.builder()
                .code(CommonErrorCode.MISSING_REQUIRED_FIELD.getCode())
                .message(message)
                .status(HttpStatus.BAD_REQUEST.value())
                .timestamp(Instant.now())
                .traceId(traceIdProvider.getTraceId())
                .instance(getRequestPath(request))
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponseWrapper.of(error));
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        log.warn("Method not supported: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .code("METHOD_NOT_ALLOWED")
                .message(ex.getMessage())
                .status(HttpStatus.METHOD_NOT_ALLOWED.value())
                .timestamp(Instant.now())
                .traceId(traceIdProvider.getTraceId())
                .instance(getRequestPath(request))
                .build();

        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ErrorResponseWrapper.of(error));
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        log.warn("Media type not supported: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .code("UNSUPPORTED_MEDIA_TYPE")
                .message(ex.getMessage())
                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value())
                .timestamp(Instant.now())
                .traceId(traceIdProvider.getTraceId())
                .instance(getRequestPath(request))
                .build();

        return ResponseEntity
                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(ErrorResponseWrapper.of(error));
    }

    @Override
    protected ResponseEntity<Object> handleNoResourceFoundException(
            NoResourceFoundException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        log.warn("No resource found: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .code(CommonErrorCode.RESOURCE_NOT_FOUND.getCode())
                .message("The requested resource was not found")
                .status(HttpStatus.NOT_FOUND.value())
                .timestamp(Instant.now())
                .traceId(traceIdProvider.getTraceId())
                .instance(getRequestPath(request))
                .build();

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponseWrapper.of(error));
    }

    // =========================================================================
    // Catch-all Handler
    // =========================================================================

    /**
     * Catch-all handler for any unhandled exceptions.
     * Returns a generic 500 Internal Server Error without leaking stack traces.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseWrapper> handleAllUncaughtExceptions(
            Exception ex, WebRequest request) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);

        ErrorResponse error = ErrorResponse.builder()
                .code(CommonErrorCode.INTERNAL_ERROR.getCode())
                .message(CommonErrorCode.INTERNAL_ERROR.getDefaultMessage())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .timestamp(Instant.now())
                .traceId(traceIdProvider.getTraceId())
                .instance(getRequestPath(request))
                .build();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponseWrapper.of(error));
    }

    // =========================================================================
    // Helper Methods
    // =========================================================================

    /**
     * Extracts the request path from the {@link WebRequest}.
     */
    protected String getRequestPath(WebRequest request) {
        if (request instanceof ServletWebRequest servletWebRequest) {
            return servletWebRequest.getRequest().getRequestURI();
        }
        return null;
    }

    /**
     * Converts a {@link ConstraintViolation} to an {@link ErrorDetail}.
     */
    private ErrorDetail toErrorDetail(ConstraintViolation<?> violation) {
        String field = extractFieldName(violation.getPropertyPath().toString());
        return ErrorDetail.of(field, violation.getMessage(), violation.getInvalidValue());
    }

    /**
     * Extracts the field name from a property path (e.g., "createOrder.arg0.name" -> "name").
     */
    private String extractFieldName(String propertyPath) {
        int lastDot = propertyPath.lastIndexOf('.');
        return lastDot >= 0 ? propertyPath.substring(lastDot + 1) : propertyPath;
    }
}
