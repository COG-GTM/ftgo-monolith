package net.chrisrichardson.ftgo.orderservice.domain;

import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerNotFoundException;
import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerService;
import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerVerificationFailedException;
import net.chrisrichardson.ftgo.domain.*;
import net.chrisrichardson.ftgo.orderservice.web.MenuItemIdAndQuantity;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Optional;

import static net.chrisrichardson.ftgo.orderservice.RestaurantMother.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class OrderCreationValidationTest {
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
    when(restaurants.findById(AJANTA_ID)).thenReturn(Optional.of(AJANTA_RESTAURANT));
  }

  @Test
  public void shouldRejectUnknownMenuItemWithFixedMessage() {
    RuntimeException failure = createFailure("untrusted-menu-id");
    assertTrue(failure instanceof InvalidMenuItemIdException);
    assertTrue(failure instanceof IllegalArgumentException);
    assertEquals("Invalid menu item id", failure.getMessage());
    verifyZeroInteractions(consumers, orders);
  }

  @Test
  public void shouldTranslateOnlyMissingConsumer() {
    ConsumerNotFoundException missing = new ConsumerNotFoundException();
    doThrow(missing).when(consumers).validateOrderForConsumer(eq(42L), any());
    RuntimeException failure = createFailure(CHICKEN_VINDALOO_MENU_ITEM_ID);
    assertTrue(failure instanceof IllegalArgumentException);
    assertEquals("Invalid consumer id", failure.getMessage());
    assertSame(missing, failure.getCause());
    verifyZeroInteractions(orders);
  }

  @Test
  public void shouldPreserveOtherConsumerVerificationFailures() {
    ConsumerVerificationFailedException failure = new ConsumerVerificationFailedException();
    doThrow(failure).when(consumers).validateOrderForConsumer(eq(42L), any());
    assertSame(failure, createFailure(CHICKEN_VINDALOO_MENU_ITEM_ID));
    verifyZeroInteractions(orders);
  }

  @Test
  public void shouldPreserveConsumerLookupFailures() {
    RuntimeException failure = new RuntimeException("database unavailable");
    doThrow(failure).when(consumers).validateOrderForConsumer(eq(42L), any());
    assertSame(failure, createFailure(CHICKEN_VINDALOO_MENU_ITEM_ID));
    verifyZeroInteractions(orders);
  }

  @Test
  public void shouldPreserveMissingRestaurant() {
    when(restaurants.findById(AJANTA_ID)).thenReturn(Optional.empty());
    assertTrue(createFailure(CHICKEN_VINDALOO_MENU_ITEM_ID) instanceof RestaurantNotFoundException);
    verifyZeroInteractions(consumers, orders);
  }

  @Test
  public void shouldSaveValidOrder() {
    Order order = service.createOrder(42L, AJANTA_ID,
            Collections.singletonList(new MenuItemIdAndQuantity(CHICKEN_VINDALOO_MENU_ITEM_ID, 1)));
    assertEquals(CHICKEN_VINDALOO_PRICE, order.getOrderTotal());
    verify(consumers).validateOrderForConsumer(42L, CHICKEN_VINDALOO_PRICE);
    verify(orders).save(order);
  }

  @Test
  public void shouldPreservePersistenceFailures() {
    RuntimeException failure = new RuntimeException("save failed");
    when(orders.save(any(Order.class))).thenThrow(failure);
    assertSame(failure, createFailure(CHICKEN_VINDALOO_MENU_ITEM_ID));
  }

  private RuntimeException createFailure(String menuItemId) {
    try {
      service.createOrder(42L, AJANTA_ID,
              Collections.singletonList(new MenuItemIdAndQuantity(menuItemId, 1)));
      fail("Expected order creation to fail");
      return null;
    } catch (RuntimeException e) {
      return e;
    }
  }
}
