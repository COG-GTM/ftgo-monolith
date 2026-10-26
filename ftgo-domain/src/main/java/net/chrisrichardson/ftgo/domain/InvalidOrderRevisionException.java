package net.chrisrichardson.ftgo.domain;

public class InvalidOrderRevisionException extends RuntimeException {
  public InvalidOrderRevisionException(String message) {
    super(message);
  }
}
