package net.chrisrichardson.ftgo.courierservice.web;

public class CourierAccessDeniedException extends RuntimeException {

  public CourierAccessDeniedException() {
    super("The authenticated principal is not allowed to perform this operation");
  }
}
