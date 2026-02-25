package com.ftgo.domain.dto;

/**
 * Menu item DTO for cross-service communication.
 * Replaces direct MenuItem embeddable sharing between services.
 */
public class MenuItemDto {

    private String menuItemId;
    private String name;
    private String price;

    public MenuItemDto() {
    }

    public MenuItemDto(String menuItemId, String name, String price) {
        this.menuItemId = menuItemId;
        this.name = name;
        this.price = price;
    }

    public String getMenuItemId() { return menuItemId; }
    public void setMenuItemId(String menuItemId) { this.menuItemId = menuItemId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }
}
