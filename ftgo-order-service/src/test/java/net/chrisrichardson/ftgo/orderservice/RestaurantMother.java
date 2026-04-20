package net.chrisrichardson.ftgo.orderservice;

import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.orderservice.domain.client.MenuItemDTO;
import net.chrisrichardson.ftgo.orderservice.domain.client.RestaurantDTO;

import java.util.Collections;
import java.util.List;

public class RestaurantMother {
  public static final String AJANTA_RESTAURANT_NAME = "Ajanta";
  public static final long AJANTA_ID = 1L;

  public static final String CHICKEN_VINDALOO = "Chicken Vindaloo";
  public static final String CHICKEN_VINDALOO_MENU_ITEM_ID = "1";
  public static final Money CHICKEN_VINDALOO_PRICE = new Money("12.34");

  public static MenuItemDTO CHICKEN_VINDALOO_MENU_ITEM = new MenuItemDTO(CHICKEN_VINDALOO_MENU_ITEM_ID, CHICKEN_VINDALOO, CHICKEN_VINDALOO_PRICE);

  public static final List<MenuItemDTO> AJANTA_RESTAURANT_MENU_ITEMS = Collections.singletonList(new MenuItemDTO(CHICKEN_VINDALOO_MENU_ITEM_ID, CHICKEN_VINDALOO, CHICKEN_VINDALOO_PRICE));
  public static final RestaurantDTO AJANTA_RESTAURANT =
          new RestaurantDTO(AJANTA_ID, AJANTA_RESTAURANT_NAME, null, AJANTA_RESTAURANT_MENU_ITEMS);
}
