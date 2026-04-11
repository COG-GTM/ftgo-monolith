package net.chrisrichardson.ftgo.orderservice.domain;

public class ConsumerValidationException extends RuntimeException {
  
  public ConsumerValidationException(String message) {
    super(message);
  }
  
  public ConsumerValidationException(String message, Throwable cause) {
    super(message, cause);
  }
}
