package net.chrisrichardson.ftgo.orderservice.restaurant;

import net.chrisrichardson.ftgo.common.Money;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

@Component
public class RestaurantServiceProxy {

  private final RestTemplate restTemplate;
  private final String restaurantServiceUrl;

  public RestaurantServiceProxy(RestTemplate restTemplate,
                                @Value("${restaurant.service.url:http://localhost:8082}") String restaurantServiceUrl) {
    this.restTemplate = restTemplate;
    this.restaurantServiceUrl = restaurantServiceUrl;
  }

  public Optional<RestaurantInfo> findRestaurantById(Long restaurantId) {
    try {
      String url = restaurantServiceUrl + "/restaurants/" + restaurantId;
      RestaurantResponse response = restTemplate.getForObject(url, RestaurantResponse.class);
      if (response != null) {
        return Optional.of(new RestaurantInfo(response.getId(), response.getName(), response.getMenuItems()));
      }
      return Optional.empty();
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  public static class RestaurantResponse {
    private Long id;
    private String name;
    private List<MenuItemInfo> menuItems;

    public RestaurantResponse() {
    }

    public Long getId() {
      return id;
    }

    public void setId(Long id) {
      this.id = id;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public List<MenuItemInfo> getMenuItems() {
      return menuItems;
    }

    public void setMenuItems(List<MenuItemInfo> menuItems) {
      this.menuItems = menuItems;
    }
  }

  public static class MenuItemInfo {
    private String id;
    private String name;
    private Money price;

    public MenuItemInfo() {
    }

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public Money getPrice() {
      return price;
    }

    public void setPrice(Money price) {
      this.price = price;
    }
  }
}
