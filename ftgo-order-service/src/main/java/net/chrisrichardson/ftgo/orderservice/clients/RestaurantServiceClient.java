package net.chrisrichardson.ftgo.orderservice.clients;

import net.chrisrichardson.ftgo.domain.MenuItem;
import net.chrisrichardson.ftgo.domain.Restaurant;
import net.chrisrichardson.ftgo.domain.RestaurantMenu;
import net.chrisrichardson.ftgo.restaurantservice.api.GetRestaurantResponse;
import net.chrisrichardson.ftgo.restaurantservice.api.MenuItemResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Optional;

public class RestaurantServiceClient {

  private final RestTemplate restTemplate;
  private final String restaurantServiceUrl;

  public RestaurantServiceClient(RestTemplate restTemplate,
                                 @Value("${restaurant.service.url:http://localhost:8082}") String restaurantServiceUrl) {
    this.restTemplate = restTemplate;
    this.restaurantServiceUrl = restaurantServiceUrl;
  }

  public Optional<MenuItem> findMenuItem(long restaurantId, String menuItemId) {
    String url = restaurantServiceUrl + "/restaurants/" + restaurantId + "/menu-items/" + menuItemId;
    try {
      ResponseEntity<MenuItemResponse> response = restTemplate.getForEntity(url, MenuItemResponse.class);
      if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
        MenuItemResponse item = response.getBody();
        return Optional.of(new MenuItem(item.getId(), item.getName(), item.getPrice()));
      }
    } catch (HttpClientErrorException e) {
      return Optional.empty();
    }
    return Optional.empty();
  }

  public Optional<Restaurant> findRestaurant(long restaurantId) {
    String url = restaurantServiceUrl + "/restaurants/" + restaurantId;
    try {
      ResponseEntity<GetRestaurantResponse> response = restTemplate.getForEntity(url, GetRestaurantResponse.class);
      if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
        return Optional.of(new Restaurant(restaurantId, response.getBody().getName(), new RestaurantMenu(Collections.emptyList())));
      }
    } catch (HttpClientErrorException e) {
      return Optional.empty();
    }
    return Optional.empty();
  }
}
