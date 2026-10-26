package net.chrisrichardson.ftgo.orderservice.restaurant;

public class RestaurantServiceUnavailableException extends RuntimeException {

  public RestaurantServiceUnavailableException(long restaurantId, Throwable cause) {
    super("Restaurant service is unavailable, cannot get restaurant " + restaurantId, cause);
  }
}
