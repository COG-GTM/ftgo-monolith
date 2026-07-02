package net.chrisrichardson.ftgo.domain;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.common.UnsupportedStateTransitionException;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class OrderTest {

  private Order order;

  @Before
  public void setUp() {
    Restaurant restaurant = new Restaurant("Ajanta",
            new Address("1 Main St", null, "Berkeley", "CA", "94704"),
            new RestaurantMenu(Collections.emptyList()));
    order = new Order(42L, restaurant, Arrays.asList(
            new OrderLineItem("item-1", "Chicken Vindaloo", new Money("12.34"), 2)));
  }

  @Test
  public void newOrderShouldBeApproved() {
    assertEquals(OrderState.APPROVED, order.getOrderState());
    assertEquals(Long.valueOf(42), order.getConsumerId());
    assertEquals(new Money("24.68"), order.getOrderTotal());
    assertEquals(1, order.getLineItems().size());
  }

  @Test
  public void shouldTransitionThroughDeliveryLifecycle() {
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

  @Test(expected = IllegalArgumentException.class)
  public void acceptTicketShouldRejectPastReadyBy() {
    order.acceptTicket(LocalDateTime.now().minusMinutes(1));
  }

  @Test(expected = UnsupportedStateTransitionException.class)
  public void acceptTicketShouldRejectNonApprovedOrder() {
    order.acceptTicket(LocalDateTime.now().plusHours(1));
    order.acceptTicket(LocalDateTime.now().plusHours(2));
  }

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

  @Test(expected = UnsupportedStateTransitionException.class)
  public void shouldNotNotePreparingBeforeAccepting() {
    order.notePreparing();
  }

  @Test(expected = UnsupportedStateTransitionException.class)
  public void shouldNotNoteReadyForPickupBeforePreparing() {
    order.noteReadyForPickup();
  }

  @Test(expected = UnsupportedStateTransitionException.class)
  public void shouldNotNotePickedUpBeforeReady() {
    order.notePickedUp();
  }

  @Test(expected = UnsupportedStateTransitionException.class)
  public void shouldNotNoteDeliveredBeforePickedUp() {
    order.noteDelivered();
  }

  @Test
  public void shouldScheduleCourier() {
    Courier courier = new Courier(new PersonName("Pat", "Smith"),
            new Address("2 Elm St", null, "Berkeley", "CA", "94704"));
    order.schedule(courier);
    assertEquals(courier, order.getAssignedCourier());
  }
}
