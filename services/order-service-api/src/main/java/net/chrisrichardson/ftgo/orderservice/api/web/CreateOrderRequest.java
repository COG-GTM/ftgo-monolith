package net.chrisrichardson.ftgo.orderservice.api.web;

import java.util.List;

/**
 * Request DTO for creating a new order.
 * Replaces direct entity sharing across services.
 *
 * <p>Service ownership: Order Service</p>
 */
public class CreateOrderRequest {

  private long restaurantId;
  private long consumerId;
  private List<LineItem> lineItems;

  private CreateOrderRequest() {
  }

  public CreateOrderRequest(long consumerId, long restaurantId, List<LineItem> lineItems) {
    this.restaurantId = restaurantId;
    this.consumerId = consumerId;
    this.lineItems = lineItems;
  }

  public long getRestaurantId() {
    return restaurantId;
  }

  public void setRestaurantId(long restaurantId) {
    this.restaurantId = restaurantId;
  }

  public long getConsumerId() {
    return consumerId;
  }

  public void setConsumerId(long consumerId) {
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
    private int quantity;

    private LineItem() {
    }

    public LineItem(String menuItemId, int quantity) {
      this.menuItemId = menuItemId;
      this.quantity = quantity;
    }

    public String getMenuItemId() {
      return menuItemId;
    }

    public void setMenuItemId(String menuItemId) {
      this.menuItemId = menuItemId;
    }

    public int getQuantity() {
      return quantity;
    }

    public void setQuantity(int quantity) {
      this.quantity = quantity;
    }
  }
}
