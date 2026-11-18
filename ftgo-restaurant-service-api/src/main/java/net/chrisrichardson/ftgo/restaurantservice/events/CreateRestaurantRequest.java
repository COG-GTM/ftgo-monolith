package net.chrisrichardson.ftgo.restaurantservice.events;


import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.Money;

public class CreateRestaurantRequest {

  private String name;
  private RestaurantMenuDTO menu;
  private Address address;

  public CreateRestaurantRequest(String name, Address address, RestaurantMenuDTO menu) {
    this.name = name;
    this.address = address;
    this.menu = menu;
  }

  public String getName() {
    return name;
  }

  public void validate() {
    if (name == null || name.trim().isEmpty())
      throw new IllegalArgumentException("Restaurant name must not be blank");
    if (menu == null || menu.getMenuItemDTOs() == null || menu.getMenuItemDTOs().isEmpty())
      throw new IllegalArgumentException("Restaurant menu must not be empty");
    for (MenuItemDTO item : menu.getMenuItemDTOs()) {
      if (item == null || item.getPrice() == null || Money.ZERO.isGreaterThanOrEqual(item.getPrice()))
        throw new IllegalArgumentException("Menu item price must be positive");
    }
  }

  public void setName(String name) {
    this.name = name;
  }

  public RestaurantMenuDTO getMenu() {
    return menu;
  }

  public void setMenu(RestaurantMenuDTO menu) {
    this.menu = menu;
  }

  private CreateRestaurantRequest() {

  }

  public void setAddress(Address address) {
    this.address = address;
  }

  public Address getAddress() {
    return address;
  }
}
