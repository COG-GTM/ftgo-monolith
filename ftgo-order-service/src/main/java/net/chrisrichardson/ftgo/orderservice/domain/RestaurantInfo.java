package net.chrisrichardson.ftgo.orderservice.domain;

import net.chrisrichardson.ftgo.common.Money;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class RestaurantInfo {

  public static class MenuItem {
    private String id;
    private String name;
    private Money price;

    public MenuItem() {
    }

    public MenuItem(String id, String name, Money price) {
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

  private Long id;
  private String name;
  private List<MenuItem> menuItems;

  public RestaurantInfo() {
  }

  public RestaurantInfo(Long id, String name, List<MenuItem> menuItems) {
    this.id = id;
    this.name = name;
    this.menuItems = menuItems;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<MenuItem> getMenuItems() {
    return menuItems == null ? Collections.emptyList() : menuItems;
  }

  public void setMenuItems(List<MenuItem> menuItems) {
    this.menuItems = menuItems;
  }

  public Optional<MenuItem> findMenuItem(String menuItemId) {
    return getMenuItems().stream().filter(mi -> mi.getId().equals(menuItemId)).findFirst();
  }
}
