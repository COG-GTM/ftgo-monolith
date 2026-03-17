package net.chrisrichardson.ftgo.orderservice.api.web;

import net.chrisrichardson.ftgo.common.Money;

/**
 * DTO for cross-service communication representing an OrderLineItem.
 * Replaces direct entity sharing of {@code OrderLineItem} embeddable from ftgo-domain.
 *
 * <p>Service ownership: Order Service</p>
 */
public class OrderLineItemDTO {

  private String menuItemId;
  private String name;
  private Money price;
  private int quantity;

  private OrderLineItemDTO() {
  }

  public OrderLineItemDTO(String menuItemId, String name, Money price, int quantity) {
    this.menuItemId = menuItemId;
    this.name = name;
    this.price = price;
    this.quantity = quantity;
  }

  public String getMenuItemId() {
    return menuItemId;
  }

  public void setMenuItemId(String menuItemId) {
    this.menuItemId = menuItemId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Money getPrice() {
    return price;
  }

  public void setPrice(Money price) {
    this.price = price;
  }

  public int getQuantity() {
    return quantity;
  }

  public void setQuantity(int quantity) {
    this.quantity = quantity;
  }
}
