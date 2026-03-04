package net.chrisrichardson.ftgo.domain;

import java.util.Objects;

import javax.persistence.*;
import java.util.List;

@Embeddable
@Access(AccessType.FIELD)
public class RestaurantMenu {
  @Embedded
  @ElementCollection
  @CollectionTable(name = "restaurant_menu_items")
  private List<MenuItem> menuItems;

  private RestaurantMenu() {
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    RestaurantMenu that = (RestaurantMenu) o;
    return Objects.equals(menuItems, that.menuItems);
  }

  @Override
  public int hashCode() {
    return Objects.hash(menuItems);
  }

  public List<MenuItem> getMenuItems() {
    return menuItems;
  }

  @Override
  public String toString() {
    return "RestaurantMenu{menuItems=" + menuItems + "}";
  }

  public void setMenuItems(List<MenuItem> menuItems) {
    this.menuItems = menuItems;
  }

  public RestaurantMenu(List<MenuItem> menuItems) {

    this.menuItems = menuItems;
  }

}
