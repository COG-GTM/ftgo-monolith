package net.chrisrichardson.ftgo.orderservice.client;

public class ConsumerValidationFailedException extends RuntimeException {

  public ConsumerValidationFailedException(long consumerId) {
    super("Consumer validation failed for consumer " + consumerId);
  }

  public ConsumerValidationFailedException(long consumerId, Throwable cause) {
    super("Consumer validation failed for consumer " + consumerId, cause);
  }
}
