package net.chrisrichardson.ftgo.domain.dto;

import java.util.List;

/**
 * Cross-service DTO representing a RestaurantMenu.
 * Used by Order Service for menu item lookup during order creation.
 */
public class RestaurantMenuDTO {

  private long restaurantId;
  private List<MenuItemDTO> menuItems;

  public RestaurantMenuDTO() {
  }

  public RestaurantMenuDTO(long restaurantId, List<MenuItemDTO> menuItems) {
    this.restaurantId = restaurantId;
    this.menuItems = menuItems;
  }

  public long getRestaurantId() {
    return restaurantId;
  }

  public void setRestaurantId(long restaurantId) {
    this.restaurantId = restaurantId;
  }

  public List<MenuItemDTO> getMenuItems() {
    return menuItems;
  }

  public void setMenuItems(List<MenuItemDTO> menuItems) {
    this.menuItems = menuItems;
  }
}
