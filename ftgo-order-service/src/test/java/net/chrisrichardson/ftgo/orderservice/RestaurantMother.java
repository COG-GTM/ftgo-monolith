package net.chrisrichardson.ftgo.orderservice;

import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.domain.RestaurantSnapshot;
import net.chrisrichardson.ftgo.restaurantservice.api.web.GetRestaurantResponse;
import net.chrisrichardson.ftgo.restaurantservice.events.MenuItemDTO;
import net.chrisrichardson.ftgo.restaurantservice.events.RestaurantMenuDTO;

import java.util.Collections;
import java.util.List;

public class RestaurantMother {
  public static final String AJANTA_RESTAURANT_NAME = "Ajanta";
  public static final long AJANTA_ID = 1L;

  public static final String CHICKEN_VINDALOO = "Chicken Vindaloo";
  public static final String CHICKEN_VINDALOO_MENU_ITEM_ID = "1";
  public static final Money CHICKEN_VINDALOO_PRICE = new Money("12.34");

  public static final List<MenuItemDTO> AJANTA_RESTAURANT_MENU_ITEMS =
          Collections.singletonList(new MenuItemDTO(CHICKEN_VINDALOO_MENU_ITEM_ID, CHICKEN_VINDALOO, CHICKEN_VINDALOO_PRICE));

  public static final RestaurantSnapshot AJANTA_RESTAURANT_SNAPSHOT =
          new RestaurantSnapshot(AJANTA_ID, AJANTA_RESTAURANT_NAME, null, null);

  public static GetRestaurantResponse ajantaRestaurant() {
    return new GetRestaurantResponse(AJANTA_ID, AJANTA_RESTAURANT_NAME, null, new RestaurantMenuDTO(AJANTA_RESTAURANT_MENU_ITEMS));
  }
}
