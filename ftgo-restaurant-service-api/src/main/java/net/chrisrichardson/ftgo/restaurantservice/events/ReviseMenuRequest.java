package net.chrisrichardson.ftgo.restaurantservice.events;

/**
 * Request body for PUT /restaurants/{id}/menu.
 */
public class ReviseMenuRequest {

  private RestaurantMenuDTO menu;

  public ReviseMenuRequest() {
  }

  public ReviseMenuRequest(RestaurantMenuDTO menu) {
    this.menu = menu;
  }

  public RestaurantMenuDTO getMenu() {
    return menu;
  }

  public void setMenu(RestaurantMenuDTO menu) {
    this.menu = menu;
  }
}
