package net.chrisrichardson.ftgo.restaurantservice.domain;

import net.chrisrichardson.ftgo.restaurantservice.api.RestaurantDetails;
import net.chrisrichardson.ftgo.restaurantservice.api.RestaurantLookupApi;
import net.chrisrichardson.ftgo.restaurantservice.events.CreateRestaurantRequest;
import net.chrisrichardson.ftgo.restaurantservice.events.MenuItemDTO;
import net.chrisrichardson.ftgo.restaurantservice.events.RestaurantMenuDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.stream.Collectors;

@Transactional(RestaurantPersistenceConfiguration.TRANSACTION_MANAGER)
public class RestaurantService implements RestaurantLookupApi {

  @Autowired
  private RestaurantRepository restaurantRepository;

  public Restaurant create(CreateRestaurantRequest request) {
    Restaurant restaurant = new Restaurant(request.getName(), request.getAddress(), makeRestaurantMenu(request.getMenu()));
    restaurantRepository.save(restaurant);
    return restaurant;
  }

  private RestaurantMenu makeRestaurantMenu(RestaurantMenuDTO menu) {
    return new RestaurantMenu(menu.getMenuItemDTOs().stream().map(mi -> new MenuItem(mi.getId(), mi.getName(), mi.getPrice())).collect(Collectors.toList()));
  }

  public Optional<Restaurant> findById(long restaurantId) {
    return restaurantRepository.findById(restaurantId);
  }

  @Override
  @Transactional(transactionManager = RestaurantPersistenceConfiguration.TRANSACTION_MANAGER, readOnly = true)
  public Optional<RestaurantDetails> findRestaurantById(long restaurantId) {
    return restaurantRepository.findById(restaurantId).map(this::toRestaurantDetails);
  }

  private RestaurantDetails toRestaurantDetails(Restaurant restaurant) {
    return new RestaurantDetails(restaurant.getId(), restaurant.getName(), restaurant.getAddress(),
            new RestaurantMenuDTO(restaurant.getMenuItems().stream()
                    .map(mi -> new MenuItemDTO(mi.getId(), mi.getName(), mi.getPrice()))
                    .collect(Collectors.toList())));
  }
}
