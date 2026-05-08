package net.chrisrichardson.ftgo.orderservice.domain;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

public class RestaurantServiceClient {

  private final RestTemplate restTemplate;
  private final String restaurantServiceUrl;

  public RestaurantServiceClient(RestTemplate restTemplate,
                                 @Value("${services.restaurant-service.url:http://restaurant-service:8080}") String restaurantServiceUrl) {
    this.restTemplate = restTemplate;
    this.restaurantServiceUrl = restaurantServiceUrl;
  }

  public Optional<RestaurantInfo> findRestaurant(long restaurantId) {
    String url = restaurantServiceUrl + "/restaurants/" + restaurantId;
    try {
      RestaurantInfo info = restTemplate.getForObject(url, RestaurantInfo.class);
      return Optional.ofNullable(info);
    } catch (HttpClientErrorException e) {
      if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
        return Optional.empty();
      }
      throw e;
    }
  }
}
