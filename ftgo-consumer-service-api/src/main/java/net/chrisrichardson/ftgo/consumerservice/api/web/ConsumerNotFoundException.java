package net.chrisrichardson.ftgo.consumerservice.api.web;

public class ConsumerNotFoundException extends ConsumerVerificationFailedException {

  public ConsumerNotFoundException() {
  }

  public ConsumerNotFoundException(String message) {
    super(message);
  }

  public ConsumerNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}
