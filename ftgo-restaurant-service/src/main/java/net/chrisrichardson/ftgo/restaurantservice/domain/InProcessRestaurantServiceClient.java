package net.chrisrichardson.ftgo.restaurantservice.domain;

import net.chrisrichardson.ftgo.restaurantservice.api.client.RestaurantServiceClient;
import net.chrisrichardson.ftgo.restaurantservice.api.web.GetRestaurantResponse;

import java.util.Optional;

/**
 * Strangler seam: serves the {@link RestaurantServiceClient} contract from inside the monolith's JVM.
 */
public class InProcessRestaurantServiceClient implements RestaurantServiceClient {

  private final RestaurantService restaurantService;

  public InProcessRestaurantServiceClient(RestaurantService restaurantService) {
    this.restaurantService = restaurantService;
  }

  @Override
  public Optional<GetRestaurantResponse> findRestaurantById(long restaurantId) {
    return restaurantService.findById(restaurantId).map(RestaurantResponses::toGetRestaurantResponse);
  }
}
