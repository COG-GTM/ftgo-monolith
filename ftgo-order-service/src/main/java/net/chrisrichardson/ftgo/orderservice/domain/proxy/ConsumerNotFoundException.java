package net.chrisrichardson.ftgo.orderservice.domain.proxy;

public class ConsumerNotFoundException extends RuntimeException {

  public ConsumerNotFoundException(String message) {
    super(message);
  }
}
