package net.chrisrichardson.ftgo.restaurantservice.api;

import java.util.Optional;

public interface RestaurantLookupApi {

  Optional<RestaurantDetails> findRestaurantById(long restaurantId);
}
