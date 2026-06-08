package net.chrisrichardson.ftgo.orderservice.client;

/**
 * Raised when the Order service cannot successfully communicate with the
 * Restaurant microservice (network error or non-404 HTTP error).
 */
public class RestaurantServiceException extends RuntimeException {
  public RestaurantServiceException(String message, Throwable cause) {
    super(message, cause);
  }
}
