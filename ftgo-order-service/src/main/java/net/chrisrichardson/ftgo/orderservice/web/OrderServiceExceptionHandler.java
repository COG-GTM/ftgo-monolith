package net.chrisrichardson.ftgo.orderservice.web;

import net.chrisrichardson.ftgo.common.ErrorResponse;
import net.chrisrichardson.ftgo.common.UnsupportedStateTransitionException;
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
public class OrderServiceExceptionHandler {

  private static final Logger logger = LoggerFactory.getLogger(OrderServiceExceptionHandler.class);

  @ExceptionHandler(OrderNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleOrderNotFound(
          OrderNotFoundException ex, HttpServletRequest request) {
    return error(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), request);
  }

  @ExceptionHandler(RestaurantNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleRestaurantNotFound(
          RestaurantNotFoundException ex, HttpServletRequest request) {
    return error(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), request);
  }

  @ExceptionHandler(UnsupportedStateTransitionException.class)
  public ResponseEntity<ErrorResponse> handleUnsupportedStateTransition(
          UnsupportedStateTransitionException ex, HttpServletRequest request) {
    logger.warn("Invalid state transition: {}", ex.getMessage());
    return error(HttpStatus.CONFLICT, "State Transition Error", ex.getMessage(), request);
  }

  @ExceptionHandler(NoCourierAvailableException.class)
  public ResponseEntity<ErrorResponse> handleNoCourierAvailable(
          NoCourierAvailableException ex, HttpServletRequest request) {
    logger.warn("No courier available: {}", ex.getMessage());
    return error(HttpStatus.SERVICE_UNAVAILABLE, "No Courier Available", ex.getMessage(), request);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgument(
          IllegalArgumentException ex, HttpServletRequest request) {
    logger.warn("Bad request: {}", ex.getMessage());
    return error(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), request);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGenericException(
          Exception ex, HttpServletRequest request) {
    logger.error("Unhandled exception on {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage(), ex);
    return error(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
            "An unexpected error occurred", request);
  }

  private ResponseEntity<ErrorResponse> error(HttpStatus status, String reason, String message,
                                               HttpServletRequest request) {
    ErrorResponse response = new ErrorResponse(status.value(), reason, message,
            request.getRequestURI(), MDC.get("correlationId"));
    return new ResponseEntity<>(response, status);
  }
}
