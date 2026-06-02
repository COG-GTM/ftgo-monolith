package net.chrisrichardson.ftgo.orderservice.client;

/**
 * Thrown when the extracted courier microservice cannot be reached or returns an unexpected error
 * (anything other than a 404, which maps to {@code CourierNotFoundException}).
 */
public class CourierServiceUnavailableException extends RuntimeException {

  public CourierServiceUnavailableException(String message, Throwable cause) {
    super(message, cause);
  }
}
