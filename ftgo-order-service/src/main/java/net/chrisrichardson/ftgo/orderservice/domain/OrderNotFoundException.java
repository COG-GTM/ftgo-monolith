package net.chrisrichardson.ftgo.orderservice.domain;

public class OrderNotFoundException extends net.chrisrichardson.ftgo.orderservice.api.OrderNotFoundException {
  public OrderNotFoundException(Long orderId) {
    super(orderId);
  }
}
