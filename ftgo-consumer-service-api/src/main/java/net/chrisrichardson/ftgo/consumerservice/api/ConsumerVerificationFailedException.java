package net.chrisrichardson.ftgo.consumerservice.api;

public class ConsumerVerificationFailedException extends RuntimeException {

  public ConsumerVerificationFailedException() {
  }

  public ConsumerVerificationFailedException(String message) {
    super(message);
  }

  public ConsumerVerificationFailedException(String message, Throwable cause) {
    super(message, cause);
  }
}
