package net.chrisrichardson.ftgo.orderservice.client;

import net.chrisrichardson.ftgo.common.ErrorResponse;
import org.springframework.http.HttpStatus;

public class OrderServiceException extends RuntimeException {
  private final HttpStatus status;
  private final ErrorResponse errorResponse;

  public OrderServiceException(HttpStatus status, ErrorResponse errorResponse, String message) {
    super(message);
    this.status = status;
    this.errorResponse = errorResponse;
  }

  public OrderServiceException(HttpStatus status, ErrorResponse errorResponse, String message, Throwable cause) {
    super(message, cause);
    this.status = status;
    this.errorResponse = errorResponse;
  }

  public HttpStatus getStatus() {
    return status;
  }

  public ErrorResponse getErrorResponse() {
    return errorResponse;
  }
}
