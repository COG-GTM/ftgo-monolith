package net.chrisrichardson.ftgo.domain.dto;

import net.chrisrichardson.ftgo.common.Money;

/**
 * Cross-service DTO representing a MenuItem.
 * Used by Order Service for order creation and price validation.
 */
public class MenuItemDTO {

  private String id;
  private String name;
  private Money price;

  public MenuItemDTO() {
  }

  public MenuItemDTO(String id, String name, Money price) {
    this.id = id;
    this.name = name;
    this.price = price;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
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
}
