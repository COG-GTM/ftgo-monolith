package net.chrisrichardson.ftgo.courierservice.web;

public class CourierAccessDeniedException extends RuntimeException {
  public CourierAccessDeniedException() {
    super("Access denied");
  }
}
