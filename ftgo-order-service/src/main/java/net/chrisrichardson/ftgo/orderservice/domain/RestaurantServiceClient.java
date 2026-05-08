package net.chrisrichardson.ftgo.orderservice.domain;

import net.chrisrichardson.ftgo.restaurantservice.events.RestaurantDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class RestaurantServiceClient {

  private final RestTemplate restTemplate;
  private final String restaurantServiceUrl;

  public RestaurantServiceClient(RestTemplate restTemplate,
                                 @Value("${services.restaurant.url}") String restaurantServiceUrl) {
    this.restTemplate = restTemplate;
    this.restaurantServiceUrl = restaurantServiceUrl;
  }

  public RestaurantDTO findById(long restaurantId) {
    return restTemplate.getForObject(
            restaurantServiceUrl + "/restaurants/" + restaurantId,
            RestaurantDTO.class);
  }
}
