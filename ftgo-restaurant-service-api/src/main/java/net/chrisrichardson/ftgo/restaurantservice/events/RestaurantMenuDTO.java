package net.chrisrichardson.ftgo.restaurantservice.events;

import java.util.Objects;

import java.util.List;

public class RestaurantMenuDTO {
  private List<MenuItemDTO> menuItems;

  private RestaurantMenuDTO() {
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    RestaurantMenuDTO that = (RestaurantMenuDTO) o;
    return Objects.equals(menuItems, that.menuItems);
  }

  @Override
  public int hashCode() {
    return Objects.hash(menuItems);
  }

  public List<MenuItemDTO> getMenuItemDTOs() {
    return menuItems;
  }

  @Override
  public String toString() {
    return "RestaurantMenuDTO{menuItems=" + menuItems + "}";
  }

  public void setMenuItemDTOs(List<MenuItemDTO> menuItems) {
    this.menuItems = menuItems;
  }

  public RestaurantMenuDTO(List<MenuItemDTO> menuItems) {

    this.menuItems = menuItems;
  }

}
