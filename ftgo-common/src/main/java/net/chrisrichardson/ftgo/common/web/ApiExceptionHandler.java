package net.chrisrichardson.ftgo.common.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.chrisrichardson.ftgo.common.ErrorResponse;
import net.chrisrichardson.ftgo.common.tracking.OriginalRequestUri;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.io.IOException;

public abstract class ApiExceptionHandler extends ResponseEntityExceptionHandler {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Override
  protected ResponseEntity<Object> handleNoResourceFoundException(
          NoResourceFoundException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
    HttpServletResponse response = ((ServletWebRequest) request).getResponse();
    try {
      response.sendError(status.value());
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
    return null;
  }

  @Override
  protected ResponseEntity<Object> handleExceptionInternal(
          Exception ex, Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
    HttpServletRequest servletRequest = ((ServletWebRequest) request).getRequest();
    HttpStatus status = HttpStatus.resolve(statusCode.value());
    String error = status != null ? status.getReasonPhrase() : String.valueOf(statusCode.value());
    String message;
    if (statusCode.is5xxServerError()) {
      logger.error("Spring MVC exception on {} {}: {}", servletRequest.getMethod(), OriginalRequestUri.of(servletRequest), ex.getMessage(), ex);
      message = "An unexpected error occurred";
    } else {
      logger.warn("Spring MVC exception on {} {}: {}", servletRequest.getMethod(), OriginalRequestUri.of(servletRequest), ex.getMessage());
      message = ex instanceof org.springframework.web.ErrorResponse springError && springError.getBody().getDetail() != null
              ? springError.getBody().getDetail() : ex.getMessage();
    }
    ErrorResponse errorBody = new ErrorResponse(statusCode.value(), error, message,
            OriginalRequestUri.of(servletRequest), MDC.get("correlationId"));
    return new ResponseEntity<>(errorBody, headers, statusCode);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgument(
          IllegalArgumentException ex, HttpServletRequest request) {
    logger.warn("Bad request: {}", ex.getMessage());
    ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(), "Bad Request", ex.getMessage(),
            OriginalRequestUri.of(request), MDC.get("correlationId"));
    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGenericException(
          Exception ex, HttpServletRequest request) {
    logger.error("Unhandled exception on {} {}: {}", request.getMethod(), OriginalRequestUri.of(request), ex.getMessage(), ex);
    ErrorResponse error = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error",
            "An unexpected error occurred",
            OriginalRequestUri.of(request), MDC.get("correlationId"));
    return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
