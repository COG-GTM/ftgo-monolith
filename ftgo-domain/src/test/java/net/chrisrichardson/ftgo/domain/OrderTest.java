package net.chrisrichardson.ftgo.domain;

import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.common.UnsupportedStateTransitionException;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class OrderTest {

  private Restaurant restaurant;
  private List<OrderLineItem> lineItems;
  private Order order;

  @Before
  public void setUp() {
    restaurant = new Restaurant("Test Restaurant",
            new net.chrisrichardson.ftgo.common.Address("1 Main St", null, "Oakland", "CA", "94612"),
            new RestaurantMenu(Collections.singletonList(new MenuItem("1", "Chicken", new Money("12.00")))));

    lineItems = Arrays.asList(
            new OrderLineItem("1", "Chicken", new Money("12.00"), 2)
    );
    order = new Order(1L, restaurant, lineItems);
  }

  @Test
  public void shouldCreateOrderWithApprovedState() {
    assertEquals(OrderState.APPROVED, order.getOrderState());
  }

  @Test
  public void shouldCalculateOrderTotal() {
    assertEquals(new Money("24.00"), order.getOrderTotal());
  }

  @Test
  public void shouldCalculateOrderTotalWithMultipleItems() {
    List<OrderLineItem> multipleItems = Arrays.asList(
            new OrderLineItem("1", "Chicken", new Money("12.00"), 2),
            new OrderLineItem("2", "Rice", new Money("3.00"), 1)
    );
    Order multiOrder = new Order(1L, restaurant, multipleItems);
    assertEquals(new Money("27.00"), multiOrder.getOrderTotal());
  }

  @Test
  public void shouldReturnConsumerId() {
    assertEquals(Long.valueOf(1L), Long.valueOf(order.getConsumerId()));
  }

  @Test
  public void shouldReturnRestaurant() {
    assertSame(restaurant, order.getRestaurant());
  }

  @Test
  public void shouldReturnLineItems() {
    assertEquals(1, order.getLineItems().size());
    assertEquals("Chicken", order.getLineItems().get(0).getName());
  }

  // State transition tests

  @Test
  public void shouldCancelApprovedOrder() {
    order.cancel();
    assertEquals(OrderState.CANCELLED, order.getOrderState());
  }

  @Test(expected = UnsupportedStateTransitionException.class)
  public void shouldNotCancelAcceptedOrder() {
    order.acceptTicket(LocalDateTime.now().plusHours(1));
    order.cancel();
  }

  @Test
  public void shouldAcceptApprovedOrder() {
    order.acceptTicket(LocalDateTime.now().plusHours(1));
    assertEquals(OrderState.ACCEPTED, order.getOrderState());
  }

  @Test(expected = UnsupportedStateTransitionException.class)
  public void shouldNotAcceptCancelledOrder() {
    order.cancel();
    order.acceptTicket(LocalDateTime.now().plusHours(1));
  }

  @Test
  public void shouldTransitionAcceptedToPreparing() {
    order.acceptTicket(LocalDateTime.now().plusHours(1));
    order.notePreparing();
    assertEquals(OrderState.PREPARING, order.getOrderState());
  }

  @Test(expected = UnsupportedStateTransitionException.class)
  public void shouldNotPrepareApprovedOrder() {
    order.notePreparing();
  }

  @Test
  public void shouldTransitionPreparingToReadyForPickup() {
    order.acceptTicket(LocalDateTime.now().plusHours(1));
    order.notePreparing();
    order.noteReadyForPickup();
    assertEquals(OrderState.READY_FOR_PICKUP, order.getOrderState());
  }

  @Test(expected = UnsupportedStateTransitionException.class)
  public void shouldNotMarkReadyForPickupFromAccepted() {
    order.acceptTicket(LocalDateTime.now().plusHours(1));
    order.noteReadyForPickup();
  }

  @Test
  public void shouldTransitionReadyForPickupToPickedUp() {
    order.acceptTicket(LocalDateTime.now().plusHours(1));
    order.notePreparing();
    order.noteReadyForPickup();
    order.notePickedUp();
    assertEquals(OrderState.PICKED_UP, order.getOrderState());
  }

  @Test(expected = UnsupportedStateTransitionException.class)
  public void shouldNotPickUpFromPreparing() {
    order.acceptTicket(LocalDateTime.now().plusHours(1));
    order.notePreparing();
    order.notePickedUp();
  }

  @Test
  public void shouldTransitionPickedUpToDelivered() {
    order.acceptTicket(LocalDateTime.now().plusHours(1));
    order.notePreparing();
    order.noteReadyForPickup();
    order.notePickedUp();
    order.noteDelivered();
    assertEquals(OrderState.DELIVERED, order.getOrderState());
  }

  @Test(expected = UnsupportedStateTransitionException.class)
  public void shouldNotDeliverFromReadyForPickup() {
    order.acceptTicket(LocalDateTime.now().plusHours(1));
    order.notePreparing();
    order.noteReadyForPickup();
    order.noteDelivered();
  }

  @Test
  public void shouldScheduleCourier() {
    Courier courier = new Courier(
            new net.chrisrichardson.ftgo.common.PersonName("Test", "Courier"),
            new net.chrisrichardson.ftgo.common.Address("1 Main St", null, "Oakland", "CA", "94612"));
    order.schedule(courier);
    assertSame(courier, order.getAssignedCourier());
  }

  @Test
  public void shouldHaveNullCourierByDefault() {
    assertNull(order.getAssignedCourier());
  }

  @Test
  public void shouldCompleteFullLifecycle() {
    assertEquals(OrderState.APPROVED, order.getOrderState());
    order.acceptTicket(LocalDateTime.now().plusHours(1));
    assertEquals(OrderState.ACCEPTED, order.getOrderState());
    order.notePreparing();
    assertEquals(OrderState.PREPARING, order.getOrderState());
    order.noteReadyForPickup();
    assertEquals(OrderState.READY_FOR_PICKUP, order.getOrderState());
    order.notePickedUp();
    assertEquals(OrderState.PICKED_UP, order.getOrderState());
    order.noteDelivered();
    assertEquals(OrderState.DELIVERED, order.getOrderState());
  }
}
