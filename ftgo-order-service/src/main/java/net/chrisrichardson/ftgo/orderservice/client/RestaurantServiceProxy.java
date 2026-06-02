package net.chrisrichardson.ftgo.orderservice.client;

import net.chrisrichardson.ftgo.domain.MenuItem;
import net.chrisrichardson.ftgo.domain.Restaurant;
import net.chrisrichardson.ftgo.domain.RestaurantMenu;
import net.chrisrichardson.ftgo.restaurantservice.events.GetRestaurantWithMenuResponse;
import net.chrisrichardson.ftgo.restaurantservice.events.MenuItemDTO;
import net.chrisrichardson.ftgo.restaurantservice.events.RestaurantNotFoundException;
import net.chrisrichardson.ftgo.restaurantservice.events.RestaurantServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

/**
 * HTTP client proxy that fetches restaurant data from the extracted
 * ftgo-restaurant-service over REST instead of via a direct repository call.
 */
public class RestaurantServiceProxy {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final RestTemplate restTemplate;
  private final String restaurantServiceUrl;

  public RestaurantServiceProxy(RestTemplate restTemplate, String restaurantServiceUrl) {
    this.restTemplate = restTemplate;
    this.restaurantServiceUrl = restaurantServiceUrl;
  }

  public Restaurant findRestaurant(long restaurantId) {
    String url = restaurantServiceUrl + "/restaurants/" + restaurantId + "/with-menu";
    logger.debug("Fetching restaurant {} with menu from {}", restaurantId, url);

    try {
      ResponseEntity<GetRestaurantWithMenuResponse> response =
              restTemplate.getForEntity(url, GetRestaurantWithMenuResponse.class);

      GetRestaurantWithMenuResponse body = response.getBody();
      if (response.getStatusCode() != HttpStatus.OK || body == null) {
        throw new RestaurantNotFoundException(restaurantId);
      }
      logger.debug("Fetched restaurant {}", restaurantId);
      return toRestaurant(body);
    } catch (HttpClientErrorException e) {
      if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
        logger.warn("Restaurant {} not found", restaurantId);
        throw new RestaurantNotFoundException(restaurantId);
      }
      logger.error("Error fetching restaurant {}: {}", restaurantId, e.getMessage());
      throw new RestaurantServiceException("Error fetching restaurant " + restaurantId, e);
    } catch (ResourceAccessException e) {
      logger.error("Restaurant service unavailable while fetching {}: {}", restaurantId, e.getMessage());
      throw new RestaurantServiceException("Restaurant service unavailable while fetching restaurant " + restaurantId, e);
    } catch (RestClientException e) {
      logger.error("Unexpected error fetching restaurant {}: {}", restaurantId, e.getMessage());
      throw new RestaurantServiceException("Unexpected error fetching restaurant " + restaurantId, e);
    }
  }

  private Restaurant toRestaurant(GetRestaurantWithMenuResponse response) {
    List<MenuItem> menuItems = response.getMenu().getMenuItemDTOs().stream()
            .map(this::toMenuItem)
            .collect(Collectors.toList());
    return new Restaurant(response.getId(), response.getName(), response.getAddress(),
            new RestaurantMenu(menuItems));
  }

  private MenuItem toMenuItem(MenuItemDTO dto) {
    return new MenuItem(dto.getId(), dto.getName(), dto.getPrice());
  }
}
