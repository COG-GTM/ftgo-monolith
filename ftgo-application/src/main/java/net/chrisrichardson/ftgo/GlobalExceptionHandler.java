package net.chrisrichardson.ftgo;

import net.chrisrichardson.ftgo.common.ErrorResponse;
import net.chrisrichardson.ftgo.common.UnsupportedStateTransitionException;
import net.chrisrichardson.ftgo.courierservice.domain.CourierNotFoundException;
import net.chrisrichardson.ftgo.domain.NoCourierAvailableException;
import net.chrisrichardson.ftgo.web.RequestBodyTooLargeException;
import net.chrisrichardson.ftgo.orderservice.domain.OrderNotFoundException;
import net.chrisrichardson.ftgo.orderservice.domain.RestaurantNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;

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

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleMessageNotReadable(
          HttpMessageNotReadableException ex, HttpServletRequest request) {
    for (Throwable cause = ex; cause != null; cause = cause.getCause()) {
      if (cause instanceof RequestBodyTooLargeException) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.PAYLOAD_TOO_LARGE.value(), "Payload Too Large", cause.getMessage(),
                request.getRequestURI(), MDC.get("correlationId"));
        return new ResponseEntity<>(error, HttpStatus.PAYLOAD_TOO_LARGE);
      }
    }

    ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(), "Bad Request", "Malformed request body",
            request.getRequestURI(), MDC.get("correlationId"));
    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
          MethodArgumentNotValidException ex, HttpServletRequest request) {
    String message = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining(", "));
    ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(), "Bad Request", message,
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
