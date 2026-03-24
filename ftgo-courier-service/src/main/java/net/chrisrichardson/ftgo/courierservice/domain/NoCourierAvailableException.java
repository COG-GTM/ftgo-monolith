package net.chrisrichardson.ftgo.courierservice.domain;

public class NoCourierAvailableException extends RuntimeException {
  public NoCourierAvailableException() {
    super("No courier available");
  }
}
