package net.chrisrichardson.ftgo.orderservice.client;

import net.chrisrichardson.ftgo.domain.MenuItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
public class RestaurantServiceClient {

  private final RestTemplate restTemplate;
  private final String restaurantServiceUrl;

  public RestaurantServiceClient(RestTemplate restTemplate,
                                  @Value("${restaurant.service.url}") String restaurantServiceUrl) {
    this.restTemplate = restTemplate;
    this.restaurantServiceUrl = restaurantServiceUrl;
  }

  public RestaurantInfo getRestaurant(long restaurantId) {
    String url = restaurantServiceUrl + "/restaurants/" + restaurantId;
    
    try {
      Map<String, Object> response = restTemplate.getForObject(url, Map.class);
      
      if (response == null) {
        throw new RuntimeException("Restaurant not found: " + restaurantId);
      }
      
      Long id = Long.valueOf(response.get("id").toString());
      String name = (String) response.get("name");
      List<Map<String, Object>> menuItemMaps = (List<Map<String, Object>>) response.get("menuItems");
      
      return new RestaurantInfo(id, name, menuItemMaps);
    } catch (Exception e) {
      throw new RuntimeException("Failed to fetch restaurant: " + restaurantId, e);
    }
  }

  public static class RestaurantInfo {
    private final Long id;
    private final String name;
    private final List<Map<String, Object>> menuItems;

    public RestaurantInfo(Long id, String name, List<Map<String, Object>> menuItems) {
      this.id = id;
      this.name = name;
      this.menuItems = menuItems;
    }

    public Long getId() {
      return id;
    }

    public String getName() {
      return name;
    }

    public List<Map<String, Object>> getMenuItems() {
      return menuItems;
    }

    public MenuItem findMenuItem(String menuItemId) {
      return menuItems.stream()
              .filter(mi -> menuItemId.equals(mi.get("id")))
              .findFirst()
              .map(mi -> new MenuItem(
                      (String) mi.get("id"),
                      (String) mi.get("name"),
                      parseMoneyFromMap((Map<String, Object>) mi.get("price"))
              ))
              .orElse(null);
    }
    
    private static net.chrisrichardson.ftgo.common.Money parseMoneyFromMap(Map<String, Object> priceMap) {
      if (priceMap == null) {
        return new net.chrisrichardson.ftgo.common.Money("0");
      }
      Object amount = priceMap.get("amount");
      if (amount != null) {
        return new net.chrisrichardson.ftgo.common.Money(amount.toString());
      }
      return new net.chrisrichardson.ftgo.common.Money("0");
    }
  }
}
