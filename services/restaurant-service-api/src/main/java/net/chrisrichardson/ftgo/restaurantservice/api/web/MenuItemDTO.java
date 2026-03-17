package net.chrisrichardson.ftgo.restaurantservice.api.web;

import net.chrisrichardson.ftgo.common.Money;

/**
 * DTO for cross-service communication representing a MenuItem.
 * Replaces direct entity sharing of {@code MenuItem} embeddable from ftgo-domain.
 *
 * <p>Service ownership: Restaurant Service</p>
 */
public class MenuItemDTO {

  private String id;
  private String name;
  private Money price;

  private MenuItemDTO() {
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
