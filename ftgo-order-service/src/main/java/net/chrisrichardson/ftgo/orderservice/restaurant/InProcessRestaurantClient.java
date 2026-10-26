package net.chrisrichardson.ftgo.orderservice.restaurant;

import net.chrisrichardson.ftgo.domain.Restaurant;
import net.chrisrichardson.ftgo.domain.RestaurantRepository;

import java.util.Optional;

public class InProcessRestaurantClient implements RestaurantClient {

  private final RestaurantRepository restaurantRepository;

  public InProcessRestaurantClient(RestaurantRepository restaurantRepository) {
    this.restaurantRepository = restaurantRepository;
  }

  @Override
  public Optional<Restaurant> findRestaurant(long restaurantId) {
    return restaurantRepository.findById(restaurantId);
  }
}
