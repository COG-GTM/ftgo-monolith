package net.chrisrichardson.ftgo.orderservice.clients;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import net.chrisrichardson.ftgo.restaurantservice.events.CreateRestaurantRequest;
import net.chrisrichardson.ftgo.restaurantservice.events.MenuItemDTO;
import net.chrisrichardson.ftgo.restaurantservice.events.RestaurantMenuDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

public class RestaurantServiceClient {

  private static final Logger logger = LoggerFactory.getLogger(RestaurantServiceClient.class);

  private final RestTemplate restTemplate;
  private final String restaurantServiceUrl;

  public RestaurantServiceClient(RestTemplate restTemplate,
                                  @Value("${services.restaurant.url}") String restaurantServiceUrl) {
    this.restTemplate = restTemplate;
    this.restaurantServiceUrl = restaurantServiceUrl;
  }

  @CircuitBreaker(name = "restaurantService", fallbackMethod = "findByIdFallback")
  @Retry(name = "restaurantService")
  public Optional<RestaurantDetails> findById(long restaurantId) {
    String url = restaurantServiceUrl + "/restaurants/" + restaurantId;
    try {
      ResponseEntity<RestaurantDetails> response = restTemplate.getForEntity(url, RestaurantDetails.class);
      if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
        return Optional.of(response.getBody());
      }
      return Optional.empty();
    } catch (org.springframework.web.client.HttpClientErrorException e) {
      if (e.getStatusCode().value() == 404) {
        return Optional.empty();
      }
      throw e;
    }
  }

  @SuppressWarnings("unused")
  private Optional<RestaurantDetails> findByIdFallback(long restaurantId, Exception e) {
    logger.error("Circuit breaker fallback: Restaurant service unavailable for restaurantId: {}", restaurantId, e);
    throw new RuntimeException("Restaurant service unavailable", e);
  }

  public static class RestaurantDetails {
    private long id;
    private String name;
    private List<MenuItemDTO> menuItems;

    public RestaurantDetails() {
    }

    public RestaurantDetails(long id, String name, List<MenuItemDTO> menuItems) {
      this.id = id;
      this.name = name;
      this.menuItems = menuItems;
    }

    public long getId() {
      return id;
    }

    public void setId(long id) {
      this.id = id;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public List<MenuItemDTO> getMenuItems() {
      return menuItems;
    }

    public void setMenuItems(List<MenuItemDTO> menuItems) {
      this.menuItems = menuItems;
    }

    public Optional<MenuItemDTO> findMenuItem(String menuItemId) {
      if (menuItems == null) return Optional.empty();
      return menuItems.stream().filter(mi -> mi.getId().equals(menuItemId)).findFirst();
    }
  }
}
