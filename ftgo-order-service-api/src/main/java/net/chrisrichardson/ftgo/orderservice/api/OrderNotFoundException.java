package net.chrisrichardson.ftgo.orderservice.api;

public class OrderNotFoundException extends RuntimeException {
  public OrderNotFoundException(Long orderId) {
    super("Order not found" + orderId);
  }
}
