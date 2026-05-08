package net.chrisrichardson.ftgo.orderservice.domain;

public class ConsumerValidationFailedException extends RuntimeException {

  public ConsumerValidationFailedException(String message) {
    super(message);
  }
}
