package net.chrisrichardson.ftgo.courierservice.domain;

public class CourierNotFoundException extends RuntimeException {
  public CourierNotFoundException(long courierId) {
    super("Courier not found: " + courierId);
  }
}
