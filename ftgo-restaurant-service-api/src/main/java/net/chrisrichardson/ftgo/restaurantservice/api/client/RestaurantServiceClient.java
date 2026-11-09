package net.chrisrichardson.ftgo.restaurantservice.api.client;

import net.chrisrichardson.ftgo.restaurantservice.api.web.GetRestaurantResponse;

import java.util.Optional;

/**
 * The only way other modules may read restaurant data. Implementations are selected by
 * {@code ftgo.restaurant-service.mode}: {@code in-process} (default, monolith) or {@code remote} (HTTP).
 */
public interface RestaurantServiceClient {

  String MODE_PROPERTY = "ftgo.restaurant-service.mode";
  String URL_PROPERTY = "ftgo.restaurant-service.url";
  String MODE_IN_PROCESS = "in-process";
  String MODE_REMOTE = "remote";

  Optional<GetRestaurantResponse> findRestaurantById(long restaurantId);
}
