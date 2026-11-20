package net.chrisrichardson.ftgo.restaurantservice.domain;

import net.chrisrichardson.ftgo.domain.MenuItem;
import net.chrisrichardson.ftgo.domain.Restaurant;
import net.chrisrichardson.ftgo.domain.RestaurantMenu;
import net.chrisrichardson.ftgo.domain.RestaurantRepository;
import net.chrisrichardson.ftgo.restaurantservice.events.CreateRestaurantRequest;
import net.chrisrichardson.ftgo.restaurantservice.events.RestaurantMenuDTO;
import net.chrisrichardson.ftgo.restaurantservice.events.MenuItemDTO;
import net.chrisrichardson.ftgo.common.Money;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.math.BigDecimal;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.stream.Collectors;

@Transactional
public class RestaurantService {

  @Autowired
  private RestaurantRepository restaurantRepository;

  @PersistenceContext
  private EntityManager entityManager;

  public Restaurant create(CreateRestaurantRequest request) {
    validate(request);
    Restaurant restaurant = new Restaurant(request.getName(), request.getAddress(), makeRestaurantMenu(request.getMenu()));
    restaurantRepository.save(restaurant);
    return restaurant;
  }

  private void validate(CreateRestaurantRequest request) {
    if (request == null || isBlank(request.getName()))
      throw new IllegalArgumentException("Restaurant name must not be blank");
    RestaurantMenuDTO menu = request.getMenu();
    if (menu == null || menu.getMenuItemDTOs() == null || menu.getMenuItemDTOs().isEmpty())
      throw new IllegalArgumentException("Restaurant menu must contain at least one item");
    Set<String> ids = new HashSet<>();
    for (MenuItemDTO item : menu.getMenuItemDTOs()) {
      if (item == null)
        throw new IllegalArgumentException("Menu item must not be null");
      if (isBlank(item.getId()))
        throw new IllegalArgumentException("Menu item id must not be blank");
      if (isBlank(item.getName()))
        throw new IllegalArgumentException("Menu item name must not be blank");
      if (item.getPrice() == null)
        throw new IllegalArgumentException("Menu item price is required");
      if (!item.getPrice().isGreaterThanOrEqual(Money.ZERO))
        throw new IllegalArgumentException("Menu item price must not be negative");
      if (!ids.add(item.getId()))
        throw new IllegalArgumentException("Menu item ids must be unique");
    }
  }

  private boolean isBlank(String value) {
    return value == null || value.trim().isEmpty();
  }

  public List<MenuItemDTO> findMenuItems(long restaurantId) {
    return entityManager.createQuery(
            "select mi.id, mi.name, mi.price.amount from Restaurant r join r.menuItems mi where r.id = :id",
            Object[].class)
            .setParameter("id", restaurantId)
            .getResultList().stream()
            .filter(row -> row[0] != null || row[1] != null || row[2] != null)
            .map(row -> new MenuItemDTO((String) row[0], (String) row[1],
                    row[2] == null ? null : new Money((BigDecimal) row[2])))
            .collect(Collectors.toList());
  }

  private RestaurantMenu makeRestaurantMenu(RestaurantMenuDTO menu) {
    return new RestaurantMenu(menu.getMenuItemDTOs().stream().map(mi -> new MenuItem(mi.getId(), mi.getName(), mi.getPrice())).collect(Collectors.toList()));
  }

  public Optional<Restaurant> findById(long restaurantId) {
    return restaurantRepository.findById(restaurantId);
  }
}
