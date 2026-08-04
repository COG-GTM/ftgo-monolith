package net.chrisrichardson.ftgo.orderservice.restaurant;

import net.chrisrichardson.ftgo.domain.MenuItem;
import net.chrisrichardson.ftgo.domain.Restaurant;
import net.chrisrichardson.ftgo.domain.RestaurantMenu;
import net.chrisrichardson.ftgo.restaurantservice.api.web.GetRestaurantResponse;
import net.chrisrichardson.ftgo.restaurantservice.events.MenuItemDTO;
import net.chrisrichardson.ftgo.restaurantservice.events.RestaurantMenuDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

/**
 * Calls the extracted restaurant service over HTTP and translates its representation
 * into the restaurant model the order domain expects.
 */
public class RemoteRestaurantClient implements RestaurantClient {

  private static final Logger logger = LoggerFactory.getLogger(RemoteRestaurantClient.class);

  private final RestTemplate restTemplate;
  private final String baseUrl;

  public RemoteRestaurantClient(RestTemplate restTemplate, String baseUrl) {
    this.restTemplate = restTemplate;
    this.baseUrl = baseUrl;
  }

  @Override
  public Optional<Restaurant> findRestaurant(long restaurantId) {
    String url = baseUrl + "/restaurants/" + restaurantId;
    try {
      logger.debug("Getting restaurant {} from {}", restaurantId, url);
      return Optional.ofNullable(restTemplate.getForObject(url, GetRestaurantResponse.class)).map(this::toRestaurant);
    } catch (HttpStatusCodeException e) {
      if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
        logger.debug("Restaurant {} not found", restaurantId);
        return Optional.empty();
      }
      throw new RestaurantServiceUnavailableException(restaurantId, e);
    } catch (RestClientException e) {
      throw new RestaurantServiceUnavailableException(restaurantId, e);
    }
  }

  private Restaurant toRestaurant(GetRestaurantResponse response) {
    return new Restaurant(response.getId(), response.getName(), response.getAddress(),
            new RestaurantMenu(toMenuItems(response.getMenu())));
  }

  private List<MenuItem> toMenuItems(RestaurantMenuDTO menu) {
    if (menu == null || menu.getMenuItemDTOs() == null)
      return Collections.emptyList();
    return menu.getMenuItemDTOs().stream()
            .map(this::toMenuItem)
            .collect(toList());
  }

  private MenuItem toMenuItem(MenuItemDTO menuItem) {
    return new MenuItem(menuItem.getId(), menuItem.getName(), menuItem.getPrice());
  }
}
