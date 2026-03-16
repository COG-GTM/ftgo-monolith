package net.chrisrichardson.ftgo.errorhandling.handler;

import net.chrisrichardson.ftgo.errorhandling.ErrorCode;
import net.chrisrichardson.ftgo.errorhandling.exception.BaseException;
import net.chrisrichardson.ftgo.errorhandling.exception.ValidationException;
import net.chrisrichardson.ftgo.openapi.model.ErrorDetail;
import net.chrisrichardson.ftgo.openapi.model.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * Centralized exception handler for all FTGO REST controllers.
 *
 * <p>Catches every known exception type and returns a consistent
 * {@link ErrorResponse} body. Exceptions are logged at the appropriate level
 * (WARN for client errors, ERROR for server errors).
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ------------------------------------------------------------------
    // FTGO application exceptions (BaseException hierarchy)
    // ------------------------------------------------------------------

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            ValidationException ex, HttpServletRequest request) {

        List<ErrorDetail> details = new ArrayList<>();
        for (ValidationException.FieldError fe : ex.getFieldErrors()) {
            details.add(new ErrorDetail(fe.getField(), fe.getMessage(), fe.getCode()));
        }

        ErrorResponse body = ErrorResponse.builder()
                .code(ex.getErrorCode().getCode())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .details(details)
                .build();

        log.warn("Validation failed on {}: {}", request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(ex.getErrorCode().getHttpStatus()).body(body);
    }

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(
            BaseException ex, HttpServletRequest request) {

        ErrorCode errorCode = ex.getErrorCode();
        ErrorResponse body = ErrorResponse.builder()
                .code(errorCode.getCode())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        if (errorCode.getHttpStatus() >= 500) {
            log.error("Server error on {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        } else {
            log.warn("Client error on {}: {}", request.getRequestURI(), ex.getMessage());
        }

        return ResponseEntity.status(errorCode.getHttpStatus()).body(body);
    }

    // ------------------------------------------------------------------
    // Bean Validation (JSR-380 / @Valid)
    // ------------------------------------------------------------------

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatus status,
            WebRequest request) {

        List<ErrorDetail> details = new ArrayList<>();
        ex.getBindingResult().getFieldErrors().forEach(fe ->
                details.add(new ErrorDetail(fe.getField(), fe.getDefaultMessage(), fe.getCode())));
        ex.getBindingResult().getGlobalErrors().forEach(ge ->
                details.add(new ErrorDetail(null, ge.getDefaultMessage(), ge.getCode())));

        ErrorResponse body = ErrorResponse.builder()
                .code(ErrorCode.VALIDATION_FAILED.getCode())
                .message("Request validation failed")
                .path(getPath(request))
                .details(details)
                .build();

        log.warn("Bean validation failed on {}: {} error(s)",
                getPath(request), details.size());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // ------------------------------------------------------------------
    // Spring MVC standard exceptions
    // ------------------------------------------------------------------

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpHeaders headers,
            HttpStatus status,
            WebRequest request) {

        ErrorResponse body = ErrorResponse.builder()
                .code(ErrorCode.BAD_REQUEST.getCode())
                .message("Malformed request body")
                .path(getPath(request))
                .build();

        log.warn("Malformed request body on {}: {}", getPath(request), ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex,
            HttpHeaders headers,
            HttpStatus status,
            WebRequest request) {

        ErrorResponse body = ErrorResponse.builder()
                .code(ErrorCode.VALIDATION_FAILED.getCode())
                .message("Missing required parameter: " + ex.getParameterName())
                .path(getPath(request))
                .build();

        log.warn("Missing parameter on {}: {}", getPath(request), ex.getParameterName());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException ex,
            HttpHeaders headers,
            HttpStatus status,
            WebRequest request) {

        ErrorResponse body = ErrorResponse.builder()
                .code(ErrorCode.BAD_REQUEST.getCode())
                .message("HTTP method " + ex.getMethod() + " is not supported for this endpoint")
                .path(getPath(request))
                .build();

        log.warn("Method not supported on {}: {}", getPath(request), ex.getMethod());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(body);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex,
            HttpHeaders headers,
            HttpStatus status,
            WebRequest request) {

        ErrorResponse body = ErrorResponse.builder()
                .code(ErrorCode.BAD_REQUEST.getCode())
                .message("Media type not supported: " + ex.getContentType())
                .path(getPath(request))
                .build();

        log.warn("Unsupported media type on {}: {}", getPath(request), ex.getContentType());
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(body);
    }

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(
            NoHandlerFoundException ex,
            HttpHeaders headers,
            HttpStatus status,
            WebRequest request) {

        ErrorResponse body = ErrorResponse.builder()
                .code(ErrorCode.RESOURCE_NOT_FOUND.getCode())
                .message("No handler found for " + ex.getHttpMethod() + " " + ex.getRequestURL())
                .path(getPath(request))
                .build();

        log.warn("No handler found: {} {}", ex.getHttpMethod(), ex.getRequestURL());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    // ------------------------------------------------------------------
    // Method argument type mismatch (e.g. wrong path variable type)
    // ------------------------------------------------------------------

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

        String requiredType = ex.getRequiredType() != null
                ? ex.getRequiredType().getSimpleName()
                : "unknown";

        ErrorResponse body = ErrorResponse.builder()
                .code(ErrorCode.BAD_REQUEST.getCode())
                .message("Parameter '" + ex.getName() + "' must be of type " + requiredType)
                .path(request.getRequestURI())
                .build();

        log.warn("Type mismatch on {}: parameter '{}' expected type {}",
                request.getRequestURI(), ex.getName(), requiredType);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // ------------------------------------------------------------------
    // Catch-all for unexpected exceptions
    // ------------------------------------------------------------------

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllUncaughtExceptions(
            Exception ex, HttpServletRequest request) {

        ErrorResponse body = ErrorResponse.builder()
                .code(ErrorCode.INTERNAL_ERROR.getCode())
                .message(ErrorCode.INTERNAL_ERROR.getDefaultMessage())
                .path(request.getRequestURI())
                .build();

        log.error("Unhandled exception on {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private static String getPath(WebRequest request) {
        if (request instanceof ServletWebRequest) {
            return ((ServletWebRequest) request).getRequest().getRequestURI();
        }
        return null;
    }
}
