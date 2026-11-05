package net.chrisrichardson.ftgo.courierservice.web;

import net.chrisrichardson.ftgo.common.ErrorResponse;
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
public class CourierSecurityExceptionHandler {

  @ExceptionHandler(CourierAuthenticationException.class)
  public ResponseEntity<ErrorResponse> handleAuthentication(
          CourierAuthenticationException ex, HttpServletRequest request) {
    ErrorResponse error = new ErrorResponse(
            HttpStatus.UNAUTHORIZED.value(), "Unauthorized", ex.getMessage(),
            request.getRequestURI(), MDC.get("correlationId"));
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .header(HttpHeaders.WWW_AUTHENTICATE, "Bearer realm=\"ftgo\"")
            .body(error);
  }

  @ExceptionHandler(CourierAccessDeniedException.class)
  public ResponseEntity<ErrorResponse> handleAccessDenied(
          CourierAccessDeniedException ex, HttpServletRequest request) {
    ErrorResponse error = new ErrorResponse(
            HttpStatus.FORBIDDEN.value(), "Forbidden", ex.getMessage(),
            request.getRequestURI(), MDC.get("correlationId"));
    return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
  }
}
