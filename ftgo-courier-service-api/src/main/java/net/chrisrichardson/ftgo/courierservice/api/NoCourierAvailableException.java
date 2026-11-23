package net.chrisrichardson.ftgo.courierservice.api;

public class NoCourierAvailableException extends RuntimeException {
  public NoCourierAvailableException() {
    super("No courier available for assignment");
  }
}
