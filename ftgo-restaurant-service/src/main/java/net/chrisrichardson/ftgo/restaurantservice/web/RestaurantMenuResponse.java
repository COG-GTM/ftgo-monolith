package net.chrisrichardson.ftgo.restaurantservice.web;

import java.util.List;

public class RestaurantMenuResponse {
    private Long restaurantId;
    private String restaurantName;
    private List<MenuItemResponse> menuItems;

    public RestaurantMenuResponse() {
    }

    public RestaurantMenuResponse(Long restaurantId, String restaurantName, List<MenuItemResponse> menuItems) {
        this.restaurantId = restaurantId;
        this.restaurantName = restaurantName;
        this.menuItems = menuItems;
    }

    public Long getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(Long restaurantId) {
        this.restaurantId = restaurantId;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public List<MenuItemResponse> getMenuItems() {
        return menuItems;
    }

    public void setMenuItems(List<MenuItemResponse> menuItems) {
        this.menuItems = menuItems;
    }
}
