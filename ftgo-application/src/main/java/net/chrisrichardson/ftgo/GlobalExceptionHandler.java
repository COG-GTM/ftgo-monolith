package net.chrisrichardson.ftgo;

import net.chrisrichardson.ftgo.common.ErrorResponse;
import net.chrisrichardson.ftgo.common.UnsupportedStateTransitionException;
import net.chrisrichardson.ftgo.courierservice.domain.CourierNotFoundException;
import net.chrisrichardson.ftgo.domain.NoCourierAvailableException;
import net.chrisrichardson.ftgo.orderservice.domain.OrderNotFoundException;
import net.chrisrichardson.ftgo.orderservice.domain.RestaurantNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Uses {@link net.chrisrichardson.ftgo.common.ErrorResponse} (not {@link org.springframework.web.ErrorResponse})
 * as the body for application errors and for Spring MVC 4xx exceptions handled by
 * {@link ResponseEntityExceptionHandler}.
 */
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  /**
   * Unknown static paths keep the Boot container error body (/error), as before Spring 6 started
   * raising NoResourceFoundException for them.
   */
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
      logger.error("Spring MVC exception on {} {}: {}", servletRequest.getMethod(), servletRequest.getRequestURI(), ex.getMessage(), ex);
      message = "An unexpected error occurred";
    } else {
      logger.warn("Spring MVC exception on {} {}: {}", servletRequest.getMethod(), servletRequest.getRequestURI(), ex.getMessage());
      message = ex instanceof org.springframework.web.ErrorResponse springError && springError.getBody().getDetail() != null
              ? springError.getBody().getDetail() : ex.getMessage();
    }
    ErrorResponse errorBody = new ErrorResponse(statusCode.value(), error, message,
            servletRequest.getRequestURI(), MDC.get("correlationId"));
    return new ResponseEntity<>(errorBody, headers, statusCode);
  }

  @ExceptionHandler(OrderNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleOrderNotFound(
          OrderNotFoundException ex, HttpServletRequest request) {
    ErrorResponse error = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(), "Not Found", ex.getMessage(),
            request.getRequestURI(), MDC.get("correlationId"));
    return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(RestaurantNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleRestaurantNotFound(
          RestaurantNotFoundException ex, HttpServletRequest request) {
    ErrorResponse error = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(), "Not Found", ex.getMessage(),
            request.getRequestURI(), MDC.get("correlationId"));
    return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(CourierNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleCourierNotFound(
          CourierNotFoundException ex, HttpServletRequest request) {
    ErrorResponse error = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(), "Not Found", ex.getMessage(),
            request.getRequestURI(), MDC.get("correlationId"));
    return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(UnsupportedStateTransitionException.class)
  public ResponseEntity<ErrorResponse> handleUnsupportedStateTransition(
          UnsupportedStateTransitionException ex, HttpServletRequest request) {
    logger.warn("Invalid state transition: {}", ex.getMessage());
    ErrorResponse error = new ErrorResponse(
            HttpStatus.CONFLICT.value(), "State Transition Error", ex.getMessage(),
            request.getRequestURI(), MDC.get("correlationId"));
    return new ResponseEntity<>(error, HttpStatus.CONFLICT);
  }

  @ExceptionHandler(NoCourierAvailableException.class)
  public ResponseEntity<ErrorResponse> handleNoCourierAvailable(
          NoCourierAvailableException ex, HttpServletRequest request) {
    logger.warn("No courier available: {}", ex.getMessage());
    ErrorResponse error = new ErrorResponse(
            HttpStatus.SERVICE_UNAVAILABLE.value(), "No Courier Available", ex.getMessage(),
            request.getRequestURI(), MDC.get("correlationId"));
    return new ResponseEntity<>(error, HttpStatus.SERVICE_UNAVAILABLE);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgument(
          IllegalArgumentException ex, HttpServletRequest request) {
    logger.warn("Bad request: {}", ex.getMessage());
    ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(), "Bad Request", ex.getMessage(),
            request.getRequestURI(), MDC.get("correlationId"));
    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGenericException(
          Exception ex, HttpServletRequest request) {
    logger.error("Unhandled exception on {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage(), ex);
    ErrorResponse error = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error",
            "An unexpected error occurred",
            request.getRequestURI(), MDC.get("correlationId"));
    return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
