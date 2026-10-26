package net.chrisrichardson.ftgo.consumerservice.api;

public class ConsumerNotFoundException extends ConsumerVerificationFailedException {

  public ConsumerNotFoundException() {
  }

  public ConsumerNotFoundException(String message) {
    super(message);
  }
}
