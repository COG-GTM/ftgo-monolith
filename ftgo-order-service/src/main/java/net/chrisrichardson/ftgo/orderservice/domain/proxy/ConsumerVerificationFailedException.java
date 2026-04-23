package net.chrisrichardson.ftgo.orderservice.domain.proxy;

public class ConsumerVerificationFailedException extends RuntimeException {

  public ConsumerVerificationFailedException(String message) {
    super(message);
  }
}
