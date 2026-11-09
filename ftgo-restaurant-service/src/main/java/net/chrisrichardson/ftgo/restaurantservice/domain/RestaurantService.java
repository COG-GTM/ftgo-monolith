package net.chrisrichardson.ftgo.restaurantservice.domain;

import net.chrisrichardson.ftgo.restaurantservice.events.CreateRestaurantRequest;
import net.chrisrichardson.ftgo.restaurantservice.events.RestaurantMenuDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

@Transactional
public class RestaurantService {

  private static final Logger logger = LoggerFactory.getLogger(RestaurantService.class);

  private final RestaurantRepository restaurantRepository;

  public RestaurantService(RestaurantRepository restaurantRepository) {
    this.restaurantRepository = restaurantRepository;
  }

  public Restaurant create(CreateRestaurantRequest request) {
    Restaurant restaurant = new Restaurant(request.getName(), request.getAddress(), makeRestaurantMenu(request.getMenu()));
    restaurantRepository.save(restaurant);
    logger.info("Created restaurant {} with {} menu items", restaurant.getId(), restaurant.getMenuItems().size());
    return restaurant;
  }

  static RestaurantMenu makeRestaurantMenu(RestaurantMenuDTO menu) {
    if (menu == null || menu.getMenuItemDTOs() == null)
      return new RestaurantMenu(Collections.emptyList());
    return new RestaurantMenu(menu.getMenuItemDTOs().stream()
            .map(mi -> new MenuItem(mi.getId(), mi.getName(), mi.getPrice()))
            .collect(Collectors.toList()));
  }

  @Transactional(readOnly = true)
  public Optional<Restaurant> findById(long restaurantId) {
    return restaurantRepository.findById(restaurantId);
  }
}
