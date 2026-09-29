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

import static org.junit.Assert.*;

public class OrderTest {

  private static final long CONSUMER_ID = 1L;

  private Restaurant restaurant;
  private Order order;

  @Before
  public void setUp() {
    restaurant = new Restaurant("Ajanta", new Address("1 Main St", null, "Oakland", "CA", "94612"),
            new RestaurantMenu(Collections.emptyList()));
    order = new Order(CONSUMER_ID, restaurant, Arrays.asList(
            new OrderLineItem("1", "Chicken Vindaloo", new Money("12.34"), 2),
            new OrderLineItem("2", "Naan", new Money("3.00"), 3)));
  }

  @Test
  public void shouldCreateOrderInApprovedState() {
    assertEquals(OrderState.APPROVED, order.getOrderState());
    assertEquals(Long.valueOf(CONSUMER_ID), order.getConsumerId());
    assertSame(restaurant, order.getRestaurant());
    assertEquals(2, order.getLineItems().size());
  }

  @Test
  public void shouldCalculateOrderTotal() {
    assertEquals(new Money("33.68"), order.getOrderTotal());
  }

  @Test
  public void shouldCancelApprovedOrder() {
    order.cancel();

    assertEquals(OrderState.CANCELLED, order.getOrderState());
  }

  @Test
  public void shouldRejectCancelWhenNotApproved() {
    order.acceptTicket(inOneHour());

    try {
      order.cancel();
      fail("expected UnsupportedStateTransitionException");
    } catch (UnsupportedStateTransitionException e) {
      assertEquals(OrderState.ACCEPTED, order.getOrderState());
    }
  }

  @Test
  public void shouldAcceptTicket() {
    order.acceptTicket(inOneHour());

    assertEquals(OrderState.ACCEPTED, order.getOrderState());
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldRejectAcceptTicketWhenReadyByNotInFuture() {
    order.acceptTicket(LocalDateTime.now().minusMinutes(1));
  }

  @Test(expected = UnsupportedStateTransitionException.class)
  public void shouldRejectAcceptTicketWhenNotApproved() {
    order.cancel();

    order.acceptTicket(inOneHour());
  }

  @Test
  public void shouldTransitionPreparingOnlyFromAccepted() {
    assertTransitionRejected(order::notePreparing);

    order.acceptTicket(inOneHour());
    order.notePreparing();

    assertEquals(OrderState.PREPARING, order.getOrderState());
  }

  @Test
  public void shouldTransitionReadyOnlyFromPreparing() {
    order.acceptTicket(inOneHour());
    assertTransitionRejected(order::noteReadyForPickup);

    order.notePreparing();
    order.noteReadyForPickup();

    assertEquals(OrderState.READY_FOR_PICKUP, order.getOrderState());
  }

  @Test
  public void shouldTransitionPickedUpOnlyFromReady() {
    order.acceptTicket(inOneHour());
    order.notePreparing();
    assertTransitionRejected(order::notePickedUp);

    order.noteReadyForPickup();
    order.notePickedUp();

    assertEquals(OrderState.PICKED_UP, order.getOrderState());
  }

  @Test
  public void shouldTransitionDeliveredOnlyFromPickedUp() {
    order.acceptTicket(inOneHour());
    order.notePreparing();
    order.noteReadyForPickup();
    assertTransitionRejected(order::noteDelivered);

    order.notePickedUp();
    order.noteDelivered();

    assertEquals(OrderState.DELIVERED, order.getOrderState());
  }

  @Test
  public void shouldScheduleCourier() {
    Courier courier = new Courier(new PersonName("Jane", "Doe"), null);

    order.schedule(courier);

    assertSame(courier, order.getAssignedCourier());
  }

  @Test
  public void shouldFollowFullHappyPath() {
    order.acceptTicket(inOneHour());
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

  private LocalDateTime inOneHour() {
    return LocalDateTime.now().plusHours(1);
  }

  private void assertTransitionRejected(Runnable transition) {
    OrderState before = order.getOrderState();
    try {
      transition.run();
      fail("expected UnsupportedStateTransitionException from state " + before);
    } catch (UnsupportedStateTransitionException e) {
      assertEquals(before, order.getOrderState());
    }
  }
}
