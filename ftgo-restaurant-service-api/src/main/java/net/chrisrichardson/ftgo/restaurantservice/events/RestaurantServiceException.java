package net.chrisrichardson.ftgo.restaurantservice.events;

public class RestaurantServiceException extends RuntimeException {
  public RestaurantServiceException(String message, Throwable cause) {
    super(message, cause);
  }
}
