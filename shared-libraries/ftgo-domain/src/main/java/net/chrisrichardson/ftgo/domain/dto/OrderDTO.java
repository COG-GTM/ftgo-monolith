package net.chrisrichardson.ftgo.domain.dto;

import net.chrisrichardson.ftgo.common.Money;

import java.util.List;

/**
 * Cross-service DTO representing an Order.
 * Used by Courier Service for delivery scheduling and Consumer Service for order history.
 */
public class OrderDTO {

  private long id;
  private String state;
  private long consumerId;
  private long restaurantId;
  private List<OrderLineItemDTO> lineItems;
  private Money orderTotal;

  public OrderDTO() {
  }

  public OrderDTO(long id, String state, long consumerId, long restaurantId,
                  List<OrderLineItemDTO> lineItems, Money orderTotal) {
    this.id = id;
    this.state = state;
    this.consumerId = consumerId;
    this.restaurantId = restaurantId;
    this.lineItems = lineItems;
    this.orderTotal = orderTotal;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
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
