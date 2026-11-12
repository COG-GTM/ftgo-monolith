package net.chrisrichardson.ftgo.courierservice.web;

import net.chrisrichardson.ftgo.common.ErrorResponse;
import net.chrisrichardson.ftgo.courierservice.domain.CourierNotFoundException;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice(assignableTypes = CourierController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CourierWebExceptionHandler {

  @ExceptionHandler(CourierAuthenticationException.class)
  public ResponseEntity<ErrorResponse> handleAuthentication(
          CourierAuthenticationException ex, HttpServletRequest request) {
    HttpHeaders headers = new HttpHeaders();
    headers.set(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
    return response(HttpStatus.UNAUTHORIZED, "Unauthorized", ex.getMessage(), request, headers);
  }

  @ExceptionHandler(CourierAccessDeniedException.class)
  public ResponseEntity<ErrorResponse> handleAccessDenied(
          CourierAccessDeniedException ex, HttpServletRequest request) {
    return response(HttpStatus.FORBIDDEN, "Forbidden", ex.getMessage(), request, new HttpHeaders());
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgument(
          IllegalArgumentException ex, HttpServletRequest request) {
    return response(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), request, new HttpHeaders());
  }

  @ExceptionHandler(CourierNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleCourierNotFound(
          CourierNotFoundException ex, HttpServletRequest request) {
    return response(HttpStatus.NOT_FOUND, "Not Found", "Courier not found", request, new HttpHeaders());
  }

  private ResponseEntity<ErrorResponse> response(HttpStatus status, String error, String message,
                                                 HttpServletRequest request, HttpHeaders headers) {
    ErrorResponse body = new ErrorResponse(status.value(), error, message,
            request.getRequestURI(), MDC.get("correlationId"));
    return new ResponseEntity<>(body, headers, status);
  }
}
