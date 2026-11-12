package net.chrisrichardson.ftgo.orderservice.domain;

public class InvalidOrderException extends IllegalArgumentException {

  public InvalidOrderException(String message) {
    super(message);
  }
}
