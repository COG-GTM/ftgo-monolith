package net.chrisrichardson.ftgo.restaurantservice.domain;

import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.domain.MenuItem;
import net.chrisrichardson.ftgo.domain.Restaurant;
import net.chrisrichardson.ftgo.domain.RestaurantMenu;
import net.chrisrichardson.ftgo.domain.RestaurantRepository;
import net.chrisrichardson.ftgo.restaurantservice.events.CreateRestaurantRequest;
import net.chrisrichardson.ftgo.restaurantservice.events.MenuItemDTO;
import net.chrisrichardson.ftgo.restaurantservice.events.RestaurantMenuDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Transactional
public class RestaurantService {

  @Autowired
  private RestaurantRepository restaurantRepository;

  @PersistenceContext
  private EntityManager entityManager;

  public Restaurant create(CreateRestaurantRequest request) {
    if (!StringUtils.hasText(request.getName())) {
      throw new IllegalArgumentException("Restaurant name is required");
    }
    Restaurant restaurant = new Restaurant(request.getName(), request.getAddress(), makeRestaurantMenu(request.getMenu()));
    restaurantRepository.save(restaurant);
    return restaurant;
  }

  private RestaurantMenu makeRestaurantMenu(RestaurantMenuDTO menu) {
    if (menu == null || menu.getMenuItemDTOs() == null) {
      throw new IllegalArgumentException("Restaurant menu and menuItemDTOs are required");
    }
    return new RestaurantMenu(menu.getMenuItemDTOs().stream().map(this::makeMenuItem).collect(Collectors.toList()));
  }

  private MenuItem makeMenuItem(MenuItemDTO item) {
    if (item == null || !StringUtils.hasText(item.getId()) || !StringUtils.hasText(item.getName()) ||
            item.getPrice() == null) {
      throw new IllegalArgumentException("Menu item id, name and price are required");
    }
    if (!item.getPrice().isGreaterThanOrEqual(Money.ZERO)) {
      throw new IllegalArgumentException("Menu item price must not be negative");
    }
    return new MenuItem(item.getId(), item.getName(), item.getPrice());
  }

  public Optional<Restaurant> findById(long restaurantId) {
    return restaurantRepository.findById(restaurantId);
  }

  public List<MenuItem> findMenuItems(long restaurantId) {
    return entityManager.createQuery(
            "select item from Restaurant r join r.menuItems item where r.id = :restaurantId", MenuItem.class)
            .setParameter("restaurantId", restaurantId)
            .getResultList();
  }
}
