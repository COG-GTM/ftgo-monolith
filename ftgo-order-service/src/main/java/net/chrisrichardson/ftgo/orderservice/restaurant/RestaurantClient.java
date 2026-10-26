package net.chrisrichardson.ftgo.orderservice.restaurant;

import net.chrisrichardson.ftgo.domain.Restaurant;

import java.util.Optional;

/**
 * The order service's view of the restaurant bounded context.
 * <p>
 * Implemented either by reading the shared database in-process or by calling the extracted
 * restaurant service over HTTP, so the order domain does not know which one is in use.
 */
public interface RestaurantClient {

  Optional<Restaurant> findRestaurant(long restaurantId);
}
