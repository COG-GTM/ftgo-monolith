package net.chrisrichardson.ftgo.domain;

public class NoCourierAvailableException extends RuntimeException {
  public NoCourierAvailableException() {
    super("No courier available for assignment");
  }
}
