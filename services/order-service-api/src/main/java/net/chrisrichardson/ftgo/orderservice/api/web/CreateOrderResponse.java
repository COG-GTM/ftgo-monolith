package net.chrisrichardson.ftgo.orderservice.api.web;

/**
 * Response DTO for order creation.
 *
 * <p>Service ownership: Order Service</p>
 */
public class CreateOrderResponse {

  private long orderId;

  private CreateOrderResponse() {
  }

  public CreateOrderResponse(long orderId) {
    this.orderId = orderId;
  }

  public long getOrderId() {
    return orderId;
  }

  public void setOrderId(long orderId) {
    this.orderId = orderId;
  }
}
