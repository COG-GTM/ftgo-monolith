package net.chrisrichardson.ftgo.domain.dto;

import net.chrisrichardson.ftgo.common.Money;

/**
 * Cross-service DTO representing an OrderLineItem.
 * Used by Restaurant Service for order ticket details.
 */
public class OrderLineItemDTO {

  private String menuItemId;
  private String name;
  private Money price;
  private int quantity;

  public OrderLineItemDTO() {
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
