package net.chrisrichardson.ftgo.courierservice.web;

public class CourierAuthenticationException extends RuntimeException {
  public CourierAuthenticationException() {
    super("Authentication required");
  }
}
