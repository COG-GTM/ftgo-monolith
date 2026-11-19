package net.chrisrichardson.ftgo.restaurantservice.domain;

import net.chrisrichardson.ftgo.domain.MenuItem;
import net.chrisrichardson.ftgo.domain.Restaurant;
import net.chrisrichardson.ftgo.domain.RestaurantMenu;
import net.chrisrichardson.ftgo.domain.RestaurantRepository;
import net.chrisrichardson.ftgo.restaurantservice.events.CreateRestaurantRequest;
import net.chrisrichardson.ftgo.restaurantservice.events.MenuItemDTO;
import net.chrisrichardson.ftgo.restaurantservice.events.RestaurantMenuDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Transactional
public class RestaurantService {

  @Autowired
  private RestaurantRepository restaurantRepository;

  public Restaurant create(CreateRestaurantRequest request) {
    Restaurant restaurant = new Restaurant(request.getName(), request.getAddress(), makeRestaurantMenu(request.getMenu()));
    restaurantRepository.save(restaurant);
    return restaurant;
  }

  private RestaurantMenu makeRestaurantMenu(RestaurantMenuDTO menu) {
    if (menu == null) {
      throw new IllegalArgumentException("menu is required");
    }

    List<MenuItemDTO> items = menu.getMenuItemDTOs();
    if (items == null) {
      throw new IllegalArgumentException("menu.menuItemDTOs is required");
    }
    if (items.stream().anyMatch(Objects::isNull)) {
      throw new IllegalArgumentException("menu.menuItemDTOs must not contain null items");
    }

    return new RestaurantMenu(items.stream()
            .map(item -> new MenuItem(item.getId(), item.getName(), item.getPrice()))
            .collect(Collectors.toList()));
  }

  public Optional<Restaurant> findById(long restaurantId) {
    return restaurantRepository.findById(restaurantId);
  }

  @Transactional(readOnly = true)
  public Optional<RestaurantDetails> findDetails(long restaurantId, boolean includeMenu) {
    return findById(restaurantId).map(restaurant -> {
      RestaurantMenuDTO menu = includeMenu ? toMenuDTO(restaurant) : null;
      return new RestaurantDetails(restaurant.getId(), restaurant.getName(), menu);
    });
  }

  private RestaurantMenuDTO toMenuDTO(Restaurant restaurant) {
    return new RestaurantMenuDTO(restaurant.streamMenuItems()
            // Hibernate can materialize an all-null legacy menu item as a null entry.
            .filter(Objects::nonNull)
            .map(item -> new MenuItemDTO(item.getId(), item.getName(), item.getPrice()))
            .collect(Collectors.toList()));
  }

  public static class RestaurantDetails {
    private final Long id;
    private final String name;
    private final RestaurantMenuDTO menu;

    private RestaurantDetails(Long id, String name, RestaurantMenuDTO menu) {
      this.id = id;
      this.name = name;
      this.menu = menu;
    }

    public Long getId() {
      return id;
    }

    public String getName() {
      return name;
    }

    public RestaurantMenuDTO getMenu() {
      return menu;
    }
  }
}
