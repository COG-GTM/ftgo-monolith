package net.chrisrichardson.ftgo.orderservice.web;

import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.domain.Action;

import java.time.LocalDateTime;
import java.util.List;

public class GetOrderResponse {
  private long orderId;
  private Long consumerId;
  private List<LineItem> lineItems;
  private String state;
  private Money orderTotal;
  private String restaurantName;
  private Long assignedCourier;
  private List<Action> courierActions;
  private LocalDateTime estimatedDeliveryTime;

  private GetOrderResponse() {
  }

  public Long getConsumerId() {
    return consumerId;
  }

  public void setConsumerId(Long consumerId) {
    this.consumerId = consumerId;
  }

  public List<LineItem> getLineItems() {
    return lineItems;
  }

  public void setLineItems(List<LineItem> lineItems) {
    this.lineItems = lineItems;
  }

  public static class LineItem {
    private String menuItemId;
    private String name;
    private Money price;
    private int quantity;

    private LineItem() {
    }

    public LineItem(String menuItemId, String name, Money price, int quantity) {
      this.menuItemId = menuItemId;
      this.name = name;
      this.price = price;
      this.quantity = quantity;
    }

    public String getMenuItemId() {
      return menuItemId;
    }

    public String getName() {
      return name;
    }

    public Money getPrice() {
      return price;
    }

    public int getQuantity() {
      return quantity;
    }
  }

  public Long getAssignedCourier() {
    return assignedCourier;
  }

  public void setAssignedCourier(Long assignedCourier) {
    this.assignedCourier = assignedCourier;
  }

  public GetOrderResponse(long orderId, String state, Money orderTotal, String restaurantName,
                           Long assignedCourier, List<Action> courierActions,
                           LocalDateTime estimatedDeliveryTime) {
    this.orderId = orderId;
    this.state = state;
    this.orderTotal = orderTotal;
    this.restaurantName = restaurantName;
    this.assignedCourier = assignedCourier;
    this.courierActions = courierActions;
    this.estimatedDeliveryTime = estimatedDeliveryTime;
  }

  public Money getOrderTotal() {
    return orderTotal;
  }

  public void setOrderTotal(Money orderTotal) {
    this.orderTotal = orderTotal;
  }

  public long getOrderId() {
    return orderId;
  }

  public void setOrderId(long orderId) {
    this.orderId = orderId;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public String getRestaurantName() {
    return restaurantName;
  }

  public List<Action> getCourierActions() {
    return courierActions;
  }

  public void setCourierActions(List<Action> courierActions) {
    this.courierActions = courierActions;
  }

  public LocalDateTime getEstimatedDeliveryTime() {
    return estimatedDeliveryTime;
  }

  public void setEstimatedDeliveryTime(LocalDateTime estimatedDeliveryTime) {
    this.estimatedDeliveryTime = estimatedDeliveryTime;
  }
}
