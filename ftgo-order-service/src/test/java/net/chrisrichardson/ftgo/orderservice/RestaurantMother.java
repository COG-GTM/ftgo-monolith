package net.chrisrichardson.ftgo.orderservice;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.restaurantservice.events.MenuItemDTO;
import net.chrisrichardson.ftgo.restaurantservice.events.RestaurantDTO;
import net.chrisrichardson.ftgo.restaurantservice.events.RestaurantMenuDTO;

import java.util.Collections;
import java.util.List;

public class RestaurantMother {
  public static final String AJANTA_RESTAURANT_NAME = "Ajanta";
  public static final long AJANTA_ID = 1L;

  public static final String CHICKEN_VINDALOO = "Chicken Vindaloo";
  public static final String CHICKEN_VINDALOO_MENU_ITEM_ID = "1";
  public static final Money CHICKEN_VINDALOO_PRICE = new Money("12.34");

  public static final MenuItemDTO CHICKEN_VINDALOO_MENU_ITEM = new MenuItemDTO(CHICKEN_VINDALOO_MENU_ITEM_ID, CHICKEN_VINDALOO, CHICKEN_VINDALOO_PRICE);

  public static final List<MenuItemDTO> AJANTA_RESTAURANT_MENU_ITEMS = Collections.singletonList(
          new MenuItemDTO(CHICKEN_VINDALOO_MENU_ITEM_ID, CHICKEN_VINDALOO, CHICKEN_VINDALOO_PRICE));

  public static final Address AJANTA_ADDRESS = new Address("1 Main St", null, "Oakland", "CA", "94612", 37.8044, -122.2712);

  public static final RestaurantDTO AJANTA_RESTAURANT =
          new RestaurantDTO(AJANTA_ID, AJANTA_RESTAURANT_NAME, AJANTA_ADDRESS, AJANTA_RESTAURANT_MENU_ITEMS);
}
