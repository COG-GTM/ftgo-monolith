package net.chrisrichardson.ftgo.orderservice.domain;

public class InvalidMenuItemIdException extends IllegalArgumentException {
  public InvalidMenuItemIdException(String menuItemId) {
    super("Invalid menu item id " + menuItemId);
  }
}
