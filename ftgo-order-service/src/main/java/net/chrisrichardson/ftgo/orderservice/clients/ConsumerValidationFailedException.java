package net.chrisrichardson.ftgo.orderservice.clients;

public class ConsumerValidationFailedException extends RuntimeException {
  public ConsumerValidationFailedException(String message) {
    super(message);
  }

  public ConsumerValidationFailedException(String message, Throwable cause) {
    super(message, cause);
  }
}
