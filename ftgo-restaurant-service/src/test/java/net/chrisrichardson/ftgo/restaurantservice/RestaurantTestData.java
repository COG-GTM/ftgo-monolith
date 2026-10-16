package net.chrisrichardson.ftgo.restaurantservice;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.domain.MenuItem;
import net.chrisrichardson.ftgo.domain.Restaurant;
import net.chrisrichardson.ftgo.domain.RestaurantMenu;
import net.chrisrichardson.ftgo.restaurantservice.events.CreateRestaurantRequest;
import net.chrisrichardson.ftgo.restaurantservice.events.MenuItemDTO;
import net.chrisrichardson.ftgo.restaurantservice.events.RestaurantMenuDTO;

import java.util.List;

/**
 * Test-data "mother" for the restaurant service tests: one well-known restaurant (Ajanta) with a
 * two-item menu, available both as the REST request DTO and as the persisted domain aggregate.
 */
public final class RestaurantTestData {

  public static final long AJANTA_ID = 1L;
  public static final String AJANTA_NAME = "Ajanta";

  public static final Address AJANTA_ADDRESS = new Address("1 Main Street", "Unit 99", "Oakland", "CA", "94611");

  public static final String CHICKEN_VINDALOO_ID = "1";
  public static final String CHICKEN_VINDALOO_NAME = "Chicken Vindaloo";
  public static final Money CHICKEN_VINDALOO_PRICE = new Money("12.34");

  public static final String SAMOSA_ID = "2";
  public static final String SAMOSA_NAME = "Samosa";
  public static final Money SAMOSA_PRICE = new Money("3.50");

  private RestaurantTestData() {
  }

  /** The request body a client POSTs to {@code /restaurants} to create Ajanta. */
  public static CreateRestaurantRequest ajantaCreateRequest() {
    RestaurantMenuDTO menu = new RestaurantMenuDTO(List.of(
            new MenuItemDTO(CHICKEN_VINDALOO_ID, CHICKEN_VINDALOO_NAME, CHICKEN_VINDALOO_PRICE),
            new MenuItemDTO(SAMOSA_ID, SAMOSA_NAME, SAMOSA_PRICE)));
    return new CreateRestaurantRequest(AJANTA_NAME, AJANTA_ADDRESS, menu);
  }

  /** The already-persisted Ajanta aggregate (id assigned) that a repository lookup would return. */
  public static Restaurant ajantaRestaurant() {
    RestaurantMenu menu = new RestaurantMenu(List.of(
            new MenuItem(CHICKEN_VINDALOO_ID, CHICKEN_VINDALOO_NAME, CHICKEN_VINDALOO_PRICE),
            new MenuItem(SAMOSA_ID, SAMOSA_NAME, SAMOSA_PRICE)));
    return new Restaurant(AJANTA_ID, AJANTA_NAME, menu);
  }
}
