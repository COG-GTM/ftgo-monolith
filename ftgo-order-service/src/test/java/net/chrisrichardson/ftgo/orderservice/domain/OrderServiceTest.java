package net.chrisrichardson.ftgo.orderservice.domain;

import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerService;
import net.chrisrichardson.ftgo.domain.CourierAssignmentStrategy;
import net.chrisrichardson.ftgo.domain.CourierRepository;
import net.chrisrichardson.ftgo.domain.MenuItem;
import net.chrisrichardson.ftgo.domain.Order;
import net.chrisrichardson.ftgo.domain.OrderRepository;
import net.chrisrichardson.ftgo.domain.Restaurant;
import net.chrisrichardson.ftgo.domain.RestaurantMenu;
import net.chrisrichardson.ftgo.domain.RestaurantRepository;
import net.chrisrichardson.ftgo.orderservice.web.MenuItemIdAndQuantity;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OrderServiceTest {

  private static final long CONSUMER_ID = 1L;
  private static final long RESTAURANT_ID = 2L;

  private OrderRepository orderRepository;
  private RestaurantRepository restaurantRepository;
  private ConsumerService consumerService;
  private CourierRepository courierRepository;
  private CourierAssignmentStrategy courierAssignmentStrategy;
  private OrderService orderService;
  private Restaurant restaurant;

  @Before
  public void setUp() {
    orderRepository = mock(OrderRepository.class);
    restaurantRepository = mock(RestaurantRepository.class);
    consumerService = mock(ConsumerService.class);
    courierRepository = mock(CourierRepository.class);
    courierAssignmentStrategy = mock(CourierAssignmentStrategy.class);
    restaurant = makeRestaurant(twoMenuItems());
    when(restaurantRepository.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurant));
    orderService = new OrderService(orderRepository, restaurantRepository, Optional.empty(),
            consumerService, courierRepository, courierAssignmentStrategy);
  }

  @Test
  public void rejectsQuantitiesOutsideAllowedRangeBeforeSaving() {
    assertInvalidOrder(Collections.singletonList(lineItem("1", 0)));
    assertInvalidOrder(Collections.singletonList(lineItem("1", -1)));
    assertInvalidOrder(Collections.singletonList(lineItem("1", 101)));
    verify(orderRepository, never()).save(any(Order.class));
  }

  @Test
  public void rejectsNullEmptyAndOversizedListsBeforeRestaurantLookup() {
    assertInvalidOrder(null);
    assertInvalidOrder(Collections.<MenuItemIdAndQuantity>emptyList());
    assertInvalidOrder(makeLineItems(101, "1", 1));

    verify(restaurantRepository, never()).findById(RESTAURANT_ID);
    verify(orderRepository, never()).save(any(Order.class));
  }

  @Test
  public void rejectsDuplicateMenuItemIdsBeforeSaving() {
    assertInvalidOrder(list(lineItem("1", 1), lineItem("1", 2)));

    verify(restaurantRepository, never()).findById(RESTAURANT_ID);
    verify(orderRepository, never()).save(any(Order.class));
  }

  @Test
  public void rejectsUnknownMenuItemIdBeforeSaving() {
    try {
      orderService.createOrder(CONSUMER_ID, RESTAURANT_ID,
              Collections.singletonList(lineItem("unknown", 1)));
      fail("Expected InvalidMenuItemIdException");
    } catch (InvalidMenuItemIdException expected) {
      assertEquals("Invalid menu item id unknown", expected.getMessage());
    }

    verify(orderRepository, never()).save(any(Order.class));
  }

  @Test
  public void rejectsOrderContainingOnlyZeroPricedItems() {
    assertInvalidOrder(Collections.singletonList(lineItem("2", 1)));

    verify(orderRepository, never()).save(any(Order.class));
  }

  @Test
  public void savesValidOrderWithExpectedTotal() {
    Order order = orderService.createOrder(CONSUMER_ID, RESTAURANT_ID,
            list(lineItem("1", 2), lineItem("2", 1)));

    assertEquals(new Money("24.68"), order.getOrderTotal());
    verify(orderRepository).save(order);
  }

  @Test
  public void acceptsOneHundredDistinctLineItemsWithMaximumQuantity() {
    List<MenuItem> menuItems = new ArrayList<>();
    List<MenuItemIdAndQuantity> lineItems = new ArrayList<>();
    for (int i = 0; i < 100; i++) {
      String menuItemId = String.valueOf(i);
      menuItems.add(new MenuItem(menuItemId, "Item " + i, new Money("1.00")));
      lineItems.add(lineItem(menuItemId, 100));
    }
    restaurant = makeRestaurant(menuItems);
    when(restaurantRepository.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurant));

    Order order = orderService.createOrder(CONSUMER_ID, RESTAURANT_ID, lineItems);

    assertEquals(new Money("10000.00"), order.getOrderTotal());
    verify(orderRepository).save(order);
  }

  private void assertInvalidOrder(List<MenuItemIdAndQuantity> lineItems) {
    try {
      orderService.createOrder(CONSUMER_ID, RESTAURANT_ID, lineItems);
      fail("Expected InvalidOrderException");
    } catch (InvalidOrderException expected) {
    }
    verify(orderRepository, never()).save(any(Order.class));
  }

  private static MenuItemIdAndQuantity lineItem(String id, int quantity) {
    return new MenuItemIdAndQuantity(id, quantity);
  }

  private static List<MenuItemIdAndQuantity> makeLineItems(int count, String id, int quantity) {
    List<MenuItemIdAndQuantity> items = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      items.add(lineItem(id + i, quantity));
    }
    return items;
  }

  private static List<MenuItemIdAndQuantity> list(MenuItemIdAndQuantity... items) {
    List<MenuItemIdAndQuantity> lineItems = new ArrayList<>();
    Collections.addAll(lineItems, items);
    return lineItems;
  }

  private static List<MenuItem> twoMenuItems() {
    List<MenuItem> items = new ArrayList<>();
    items.add(new MenuItem("1", "Priced item", new Money("12.34")));
    items.add(new MenuItem("2", "Free item", new Money("0.00")));
    return items;
  }

  private static Restaurant makeRestaurant(List<MenuItem> menuItems) {
    return new Restaurant(RESTAURANT_ID, "Test restaurant", new RestaurantMenu(menuItems));
  }
}
