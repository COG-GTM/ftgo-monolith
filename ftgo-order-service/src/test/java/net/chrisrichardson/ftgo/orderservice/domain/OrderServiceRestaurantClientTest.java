package net.chrisrichardson.ftgo.orderservice.domain;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerService;
import net.chrisrichardson.ftgo.domain.CourierAssignmentStrategy;
import net.chrisrichardson.ftgo.domain.CourierRepository;
import net.chrisrichardson.ftgo.domain.Order;
import net.chrisrichardson.ftgo.domain.OrderRepository;
import net.chrisrichardson.ftgo.domain.RestaurantSnapshot;
import net.chrisrichardson.ftgo.orderservice.OrderDetailsMother;
import net.chrisrichardson.ftgo.orderservice.RestaurantMother;
import net.chrisrichardson.ftgo.orderservice.web.MenuItemIdAndQuantity;
import net.chrisrichardson.ftgo.restaurantservice.api.client.RestaurantServiceClient;
import net.chrisrichardson.ftgo.restaurantservice.api.web.GetRestaurantResponse;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class OrderServiceRestaurantClientTest {

  private RestaurantServiceClient restaurantServiceClient;
  private OrderRepository orderRepository;
  private OrderService orderService;

  @Before
  public void setUp() {
    restaurantServiceClient = mock(RestaurantServiceClient.class);
    orderRepository = mock(OrderRepository.class);
    orderService = new OrderService(orderRepository, restaurantServiceClient, Optional.empty(),
            mock(ConsumerService.class), mock(CourierRepository.class), mock(CourierAssignmentStrategy.class));
  }

  @Test
  public void shouldPriceOrderFromRestaurantContractAndKeepSnapshot() {
    GetRestaurantResponse ajanta = RestaurantMother.ajantaRestaurant();
    ajanta.setAddress(new Address("1 Main St", null, "Oakland", "CA", "94612", 37.8, -122.27));
    when(restaurantServiceClient.findRestaurantById(RestaurantMother.AJANTA_ID)).thenReturn(Optional.of(ajanta));

    Order order = orderService.createOrder(OrderDetailsMother.CONSUMER_ID, RestaurantMother.AJANTA_ID,
            OrderDetailsMother.CHICKEN_VINDALOO_MENU_ITEMS_AND_QUANTITIES);

    verify(orderRepository).save(order);
    assertEquals(OrderDetailsMother.CHICKEN_VINDALOO_ORDER_TOTAL, order.getOrderTotal());
    RestaurantSnapshot snapshot = order.getRestaurant();
    assertEquals(Long.valueOf(RestaurantMother.AJANTA_ID), snapshot.getId());
    assertEquals(RestaurantMother.AJANTA_RESTAURANT_NAME, snapshot.getName());
    assertEquals(Double.valueOf(37.8), snapshot.getLatitude());
    assertTrue(snapshot.hasLocation());
  }

  @Test(expected = RestaurantNotFoundException.class)
  public void shouldRejectUnknownRestaurant() {
    when(restaurantServiceClient.findRestaurantById(42L)).thenReturn(Optional.empty());
    orderService.createOrder(1L, 42L, OrderDetailsMother.CHICKEN_VINDALOO_MENU_ITEMS_AND_QUANTITIES);
  }

  @Test(expected = InvalidMenuItemIdException.class)
  public void shouldRejectMenuItemNotOnRestaurantMenu() {
    when(restaurantServiceClient.findRestaurantById(RestaurantMother.AJANTA_ID))
            .thenReturn(Optional.of(RestaurantMother.ajantaRestaurant()));
    orderService.createOrder(1L, RestaurantMother.AJANTA_ID,
            Collections.singletonList(new MenuItemIdAndQuantity("does-not-exist", 1)));
  }

  @Test
  public void snapshotShouldTolerateMissingAddress() {
    RestaurantSnapshot snapshot = OrderService.makeRestaurantSnapshot(1L, RestaurantMother.ajantaRestaurant());
    assertFalse(snapshot.hasLocation());
  }
}
