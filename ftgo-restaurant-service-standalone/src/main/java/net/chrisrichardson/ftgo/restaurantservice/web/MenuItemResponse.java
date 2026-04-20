package net.chrisrichardson.ftgo.restaurantservice.web;

import net.chrisrichardson.ftgo.common.Money;

public class MenuItemResponse {
  private String id;
  private String name;
  private Money price;

  public MenuItemResponse() {
  }

  public MenuItemResponse(String id, String name, Money price) {
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
