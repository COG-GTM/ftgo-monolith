package net.chrisrichardson.ftgo;

import net.chrisrichardson.ftgo.common.ErrorResponse;
import net.chrisrichardson.ftgo.common.web.CommonExceptionHandler;
import net.chrisrichardson.ftgo.courierservice.domain.CourierNotFoundException;
import net.chrisrichardson.ftgo.domain.NoCourierAvailableException;
import net.chrisrichardson.ftgo.orderservice.domain.OrderNotFoundException;
import net.chrisrichardson.ftgo.orderservice.domain.RestaurantNotFoundException;
import net.chrisrichardson.ftgo.restaurantservice.api.client.RestaurantServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
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

  @ExceptionHandler(NoCourierAvailableException.class)
  public ResponseEntity<ErrorResponse> handleNoCourierAvailable(
          NoCourierAvailableException ex, HttpServletRequest request) {
    logger.warn("No courier available: {}", ex.getMessage());
    ErrorResponse error = new ErrorResponse(
            HttpStatus.SERVICE_UNAVAILABLE.value(), "No Courier Available", ex.getMessage(),
            request.getRequestURI(), MDC.get("correlationId"));
    return new ResponseEntity<>(error, HttpStatus.SERVICE_UNAVAILABLE);
  }

  @ExceptionHandler(RestaurantServiceUnavailableException.class)
  public ResponseEntity<ErrorResponse> handleRestaurantServiceUnavailable(
          RestaurantServiceUnavailableException ex, HttpServletRequest request) {
    logger.warn("Restaurant service unavailable: {}", ex.getMessage());
    return CommonExceptionHandler.error(HttpStatus.SERVICE_UNAVAILABLE, "Restaurant Service Unavailable", ex.getMessage(), request);
  }
}
