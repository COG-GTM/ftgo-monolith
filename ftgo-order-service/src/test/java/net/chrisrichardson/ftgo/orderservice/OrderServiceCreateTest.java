package net.chrisrichardson.ftgo.orderservice;

import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerNotFoundException;
import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerService;
import net.chrisrichardson.ftgo.domain.*;
import net.chrisrichardson.ftgo.orderservice.domain.OrderService;
import net.chrisrichardson.ftgo.orderservice.domain.RestaurantNotFoundException;
import net.chrisrichardson.ftgo.orderservice.web.MenuItemIdAndQuantity;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class OrderServiceCreateTest {
  private OrderRepository orders;
  private RestaurantRepository restaurants;
  private ConsumerService consumers;
  private OrderService service;

  @Before
  public void setUp() {
    orders = mock(OrderRepository.class);
    restaurants = mock(RestaurantRepository.class);
    consumers = mock(ConsumerService.class);
    service = new OrderService(orders, restaurants, Optional.empty(), consumers,
            mock(CourierRepository.class), mock(CourierAssignmentStrategy.class));
    when(restaurants.findById(2L)).thenReturn(Optional.of(RestaurantMother.AJANTA_RESTAURANT));
  }

  @Test
  public void createsValidOrder() {
    Order order = service.createOrder(1, 2, items("1", 2));
    assertEquals(new Money("24.68"), order.getOrderTotal());
    verify(consumers).validateOrderForConsumer(1L, new Money("24.68"));
    verify(orders).save(order);
  }

  @Test
  public void rejectsMissingAndEmptyItems() {
    rejects(null, "Order must contain line items");
    rejects(Collections.emptyList(), "Order must contain line items");
  }

  @Test
  public void rejectsNonPositiveQuantities() {
    rejects(items("1", 0), "Line item quantity must be positive");
    rejects(items("1", -1), "Line item quantity must be positive");
  }

  @Test
  public void rejectsUnknownAndNullMenuIds() {
    rejects(items("private-input", 1), "Invalid menu item");
    rejects(items(null, 1), "Invalid menu item");
    rejects(Collections.singletonList(null), "Order line items must not be null");
  }

  @Test
  public void rejectsMalformedLegacyMenus() {
    for (List<MenuItem> menu : Arrays.asList(
            Collections.<MenuItem>singletonList(null),
            Collections.singletonList(new MenuItem(null, "Legacy", new Money("1"))),
            Collections.<MenuItem>emptyList())) {
      when(restaurants.findById(2L)).thenReturn(Optional.of(
              new Restaurant(2L, "Legacy", new RestaurantMenu(menu))));
      rejects(items("1", 1), "Invalid menu item");
    }
  }

  @Test
  public void translatesUnknownConsumer() {
    doThrow(new ConsumerNotFoundException()).when(consumers).validateOrderForConsumer(anyLong(), any(Money.class));
    rejects(items("1", 1), "Consumer not found");
  }

  @Test(expected = RestaurantNotFoundException.class)
  public void preservesUnknownRestaurant() {
    when(restaurants.findById(2L)).thenReturn(Optional.empty());
    service.createOrder(1, 2, items("1", 1));
  }

  @Test
  public void rejectsMissingConsumerId() {
    try {
      service.createOrder(0, 2, items("1", 1));
      fail("Expected validation failure");
    } catch (IllegalArgumentException e) {
      assertEquals("Consumer ID must be positive", e.getMessage());
    }
    verify(orders, never()).save(any(Order.class));
  }

  private List<MenuItemIdAndQuantity> items(String id, int quantity) {
    return Collections.singletonList(new MenuItemIdAndQuantity(id, quantity));
  }

  private void rejects(List<MenuItemIdAndQuantity> items, String message) {
    try {
      service.createOrder(1, 2, items);
      fail("Expected validation failure");
    } catch (IllegalArgumentException e) {
      assertEquals(message, e.getMessage());
    }
    verify(orders, never()).save(any(Order.class));
  }
}
