package net.chrisrichardson.ftgo.domain;

import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.common.UnsupportedStateTransitionException;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

public class OrderTest {

  private Restaurant restaurant;
  private Order order;

  @Before
  public void setUp() {
    restaurant = new Restaurant(1L, "Test Restaurant",
        new RestaurantMenu(Collections.singletonList(
            new MenuItem("item1", "Chicken", new Money("12.00")))));
    order = new Order(100L, restaurant,
        Arrays.asList(new OrderLineItem("item1", "Chicken", new Money("12.00"), 2)));
  }

  @Test
  public void shouldCreateOrderInApprovedState() {
    assertEquals(OrderState.APPROVED, order.getOrderState());
  }

  @Test
  public void shouldReturnConsumerId() {
    assertEquals(Long.valueOf(100L), order.getConsumerId());
  }

  @Test
  public void shouldReturnRestaurant() {
    assertSame(restaurant, order.getRestaurant());
  }

  @Test
  public void shouldReturnLineItems() {
    assertEquals(1, order.getLineItems().size());
    assertEquals("item1", order.getLineItems().get(0).getMenuItemId());
  }

  @Test
  public void shouldCalculateOrderTotal() {
    assertEquals(new Money("24.00"), order.getOrderTotal());
  }

  @Test
  public void shouldCancelApprovedOrder() {
    order.cancel();
    assertEquals(OrderState.CANCELLED, order.getOrderState());
  }

  @Test(expected = UnsupportedStateTransitionException.class)
  public void shouldNotCancelNonApprovedOrder() {
    order.cancel();
    order.cancel(); // already cancelled, should throw
  }

  @Test
  public void shouldAcceptTicket() {
    LocalDateTime readyBy = LocalDateTime.now().plusHours(1);
    order.acceptTicket(readyBy);
    assertEquals(OrderState.ACCEPTED, order.getOrderState());
  }

  @Test(expected = UnsupportedStateTransitionException.class)
  public void shouldNotAcceptTicketWhenNotApproved() {
    order.cancel();
    LocalDateTime readyBy = LocalDateTime.now().plusHours(1);
    order.acceptTicket(readyBy);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldRejectAcceptTicketWithPastReadyBy() {
    LocalDateTime readyBy = LocalDateTime.now().minusHours(1);
    order.acceptTicket(readyBy);
  }

  @Test
  public void shouldNotePreparing() {
    order.acceptTicket(LocalDateTime.now().plusHours(1));
    order.notePreparing();
    assertEquals(OrderState.PREPARING, order.getOrderState());
  }

  @Test(expected = UnsupportedStateTransitionException.class)
  public void shouldNotNotePreparingWhenNotAccepted() {
    order.notePreparing(); // still APPROVED
  }

  @Test
  public void shouldNoteReadyForPickup() {
    order.acceptTicket(LocalDateTime.now().plusHours(1));
    order.notePreparing();
    order.noteReadyForPickup();
    assertEquals(OrderState.READY_FOR_PICKUP, order.getOrderState());
  }

  @Test(expected = UnsupportedStateTransitionException.class)
  public void shouldNotNoteReadyForPickupWhenNotPreparing() {
    order.noteReadyForPickup(); // still APPROVED
  }

  @Test
  public void shouldNotePickedUp() {
    order.acceptTicket(LocalDateTime.now().plusHours(1));
    order.notePreparing();
    order.noteReadyForPickup();
    order.notePickedUp();
    assertEquals(OrderState.PICKED_UP, order.getOrderState());
  }

  @Test(expected = UnsupportedStateTransitionException.class)
  public void shouldNotNotePickedUpWhenNotReadyForPickup() {
    order.notePickedUp(); // still APPROVED
  }

  @Test
  public void shouldNoteDelivered() {
    order.acceptTicket(LocalDateTime.now().plusHours(1));
    order.notePreparing();
    order.noteReadyForPickup();
    order.notePickedUp();
    order.noteDelivered();
    assertEquals(OrderState.DELIVERED, order.getOrderState());
  }

  @Test(expected = UnsupportedStateTransitionException.class)
  public void shouldNotNoteDeliveredWhenNotPickedUp() {
    order.noteDelivered(); // still APPROVED
  }

  @Test
  public void shouldCompleteFullOrderLifecycle() {
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

  @Test
  public void shouldScheduleCourier() {
    Courier courier = new Courier();
    order.schedule(courier);
    assertSame(courier, order.getAssignedCourier());
  }

  @Test
  public void shouldSetAndGetId() {
    order.setId(42L);
    assertEquals(Long.valueOf(42L), order.getId());
  }
}
