package net.chrisrichardson.ftgo.orderservice.domain.client;

import net.chrisrichardson.ftgo.common.Money;

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
