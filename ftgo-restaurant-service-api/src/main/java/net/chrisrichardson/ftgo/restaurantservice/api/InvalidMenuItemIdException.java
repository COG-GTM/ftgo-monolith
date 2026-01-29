package net.chrisrichardson.ftgo.restaurantservice.api;

public class InvalidMenuItemIdException extends RuntimeException {
    private final String menuItemId;

    public InvalidMenuItemIdException(String menuItemId) {
        super("Invalid menu item id " + menuItemId);
        this.menuItemId = menuItemId;
    }

    public String getMenuItemId() {
        return menuItemId;
    }
}
