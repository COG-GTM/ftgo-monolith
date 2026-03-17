package net.chrisrichardson.ftgo.restaurantservice.api.web;

import java.util.List;

/**
 * DTO for cross-service communication representing a RestaurantMenu.
 * Replaces direct entity sharing of {@code RestaurantMenu} embeddable from ftgo-domain.
 *
 * <p>Service ownership: Restaurant Service</p>
 */
public class RestaurantMenuDTO {

  private List<MenuItemDTO> menuItems;

  private RestaurantMenuDTO() {
  }

  public RestaurantMenuDTO(List<MenuItemDTO> menuItems) {
    this.menuItems = menuItems;
  }

  public List<MenuItemDTO> getMenuItems() {
    return menuItems;
  }

  public void setMenuItems(List<MenuItemDTO> menuItems) {
    this.menuItems = menuItems;
  }
}
