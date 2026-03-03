package net.chrisrichardson.ftgo.orderservice.api.events;

import net.chrisrichardson.ftgo.common.Money;
import java.util.Objects;

import java.util.List;

public class OrderDetails {

  private List<OrderLineItemDTO> lineItems;
  private Money orderTotal;

  private long restaurantId;
  private long consumerId;

  private OrderDetails() {
  }

  public Money getOrderTotal() {
    return orderTotal;
  }

  public void setOrderTotal(Money orderTotal) {
    this.orderTotal = orderTotal;
  }

  public OrderDetails(long consumerId, long restaurantId, List<OrderLineItemDTO> lineItems, Money orderTotal) {
    this.consumerId = consumerId;
    this.restaurantId = restaurantId;
    this.lineItems = lineItems;
    this.orderTotal = orderTotal;
  }

  @Override
  public String toString() {
    return "OrderDetails{consumerId=" + consumerId + ", restaurantId=" + restaurantId + ", lineItems=" + lineItems + ", orderTotal=" + orderTotal + "}";
  }

  public List<OrderLineItemDTO> getLineItems() {
    return lineItems;
  }

  public long getRestaurantId() {
    return restaurantId;
  }

  public long getConsumerId() {
    return consumerId;
  }


  public void setLineItems(List<OrderLineItemDTO> lineItems) {
    this.lineItems = lineItems;
  }


  public void setRestaurantId(long restaurantId) {
    this.restaurantId = restaurantId;
  }

  public void setConsumerId(long consumerId) {
    this.consumerId = consumerId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    OrderDetails that = (OrderDetails) o;
    return restaurantId == that.restaurantId &&
            consumerId == that.consumerId &&
            Objects.equals(lineItems, that.lineItems) &&
            Objects.equals(orderTotal, that.orderTotal);
  }

  @Override
  public int hashCode() {
    return Objects.hash(lineItems, orderTotal, restaurantId, consumerId);
  }


}
