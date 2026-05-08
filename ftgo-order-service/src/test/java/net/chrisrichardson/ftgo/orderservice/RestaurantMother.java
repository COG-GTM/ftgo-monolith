package net.chrisrichardson.ftgo.orderservice;

import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.orderservice.domain.RestaurantInfo;

import java.util.Collections;
import java.util.List;

public class RestaurantMother {
  public static final String AJANTA_RESTAURANT_NAME = "Ajanta";
  public static final long AJANTA_ID = 1L;

  public static final String CHICKEN_VINDALOO = "Chicken Vindaloo";
  public static final String CHICKEN_VINDALOO_MENU_ITEM_ID = "1";
  public static final Money CHICKEN_VINDALOO_PRICE = new Money("12.34");

  public static final RestaurantInfo.MenuItem CHICKEN_VINDALOO_MENU_ITEM =
          new RestaurantInfo.MenuItem(CHICKEN_VINDALOO_MENU_ITEM_ID, CHICKEN_VINDALOO, CHICKEN_VINDALOO_PRICE);

  public static final List<RestaurantInfo.MenuItem> AJANTA_RESTAURANT_MENU_ITEMS =
          Collections.singletonList(CHICKEN_VINDALOO_MENU_ITEM);

  public static final RestaurantInfo AJANTA_RESTAURANT =
          new RestaurantInfo(AJANTA_ID, AJANTA_RESTAURANT_NAME, AJANTA_RESTAURANT_MENU_ITEMS);
}
