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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

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
