package net.chrisrichardson.ftgo.orderservice.api.web;

import net.chrisrichardson.ftgo.common.Money;

import java.util.List;

/**
 * Response DTO for retrieving order details.
 * Replaces direct entity sharing of {@code Order} entity from ftgo-domain.
 *
 * <p>Service ownership: Order Service</p>
 */
public class GetOrderResponse {

  private long orderId;
  private String orderState;
  private long consumerId;
  private long restaurantId;
  private List<OrderLineItemDTO> lineItems;
  private Money orderTotal;

  private GetOrderResponse() {
  }

  public GetOrderResponse(long orderId, String orderState, long consumerId,
                           long restaurantId, List<OrderLineItemDTO> lineItems, Money orderTotal) {
    this.orderId = orderId;
    this.orderState = orderState;
    this.consumerId = consumerId;
    this.restaurantId = restaurantId;
    this.lineItems = lineItems;
    this.orderTotal = orderTotal;
  }

  public long getOrderId() {
    return orderId;
  }

  public void setOrderId(long orderId) {
    this.orderId = orderId;
  }

  public String getOrderState() {
    return orderState;
  }

  public void setOrderState(String orderState) {
    this.orderState = orderState;
  }

  public long getConsumerId() {
    return consumerId;
  }

  public void setConsumerId(long consumerId) {
    this.consumerId = consumerId;
  }

  public long getRestaurantId() {
    return restaurantId;
  }

  public void setRestaurantId(long restaurantId) {
    this.restaurantId = restaurantId;
  }

  public List<OrderLineItemDTO> getLineItems() {
    return lineItems;
  }

  public void setLineItems(List<OrderLineItemDTO> lineItems) {
    this.lineItems = lineItems;
  }

  public Money getOrderTotal() {
    return orderTotal;
  }

  public void setOrderTotal(Money orderTotal) {
    this.orderTotal = orderTotal;
  }
}
