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

  private Restaurant restaurant;
  private Order order;

  @Before
  public void setUp() {
    MenuItem item = new MenuItem("1", "Chicken Vindaloo", new Money("12.34"));
    restaurant = new Restaurant("Ajanta",
            new Address("1 Main St", null, "Oakland", "CA", "94612"),
            new RestaurantMenu(Collections.singletonList(item)));

    order = new Order(1L, restaurant,
            Collections.singletonList(new OrderLineItem("1", "Chicken Vindaloo", new Money("12.34"), 2)));
  }

  @Test
  public void shouldCreateOrderInApprovedState() {
    assertEquals(OrderState.APPROVED, order.getOrderState());
  }

  @Test
  public void shouldCalculateOrderTotal() {
    Money expected = new Money("12.34").multiply(2);
    assertEquals(expected, order.getOrderTotal());
  }

  @Test
  public void shouldCancelApprovedOrder() {
    order.cancel();
    assertEquals(OrderState.CANCELLED, order.getOrderState());
  }

  @Test(expected = UnsupportedStateTransitionException.class)
  public void shouldRejectCancelWhenNotApproved() {
    order.acceptTicket(LocalDateTime.now().plusHours(1));
    order.cancel();
  }

  @Test
  public void shouldAcceptTicket() {
    LocalDateTime readyBy = LocalDateTime.now().plusHours(1);
    order.acceptTicket(readyBy);
    assertEquals(OrderState.ACCEPTED, order.getOrderState());
  }

  @Test(expected = UnsupportedStateTransitionException.class)
  public void shouldRejectAcceptTicketWhenNotApproved() {
    order.cancel();
    order.acceptTicket(LocalDateTime.now().plusHours(1));
  }

  @Test
  public void shouldTransitionPreparingOnlyFromAccepted() {
    order.acceptTicket(LocalDateTime.now().plusHours(1));
    order.notePreparing();
    assertEquals(OrderState.PREPARING, order.getOrderState());
  }

  @Test(expected = UnsupportedStateTransitionException.class)
  public void shouldRejectPreparingWhenNotAccepted() {
    order.notePreparing();
  }

  @Test
  public void shouldTransitionReadyOnlyFromPreparing() {
    order.acceptTicket(LocalDateTime.now().plusHours(1));
    order.notePreparing();
    order.noteReadyForPickup();
    assertEquals(OrderState.READY_FOR_PICKUP, order.getOrderState());
  }

  @Test(expected = UnsupportedStateTransitionException.class)
  public void shouldRejectReadyWhenNotPreparing() {
    order.noteReadyForPickup();
  }

  @Test
  public void shouldTransitionPickedUpOnlyFromReady() {
    order.acceptTicket(LocalDateTime.now().plusHours(1));
    order.notePreparing();
    order.noteReadyForPickup();
    order.notePickedUp();
    assertEquals(OrderState.PICKED_UP, order.getOrderState());
  }

  @Test(expected = UnsupportedStateTransitionException.class)
  public void shouldRejectPickedUpWhenNotReady() {
    order.notePickedUp();
  }

  @Test
  public void shouldTransitionDeliveredOnlyFromPickedUp() {
    order.acceptTicket(LocalDateTime.now().plusHours(1));
    order.notePreparing();
    order.noteReadyForPickup();
    order.notePickedUp();
    order.noteDelivered();
    assertEquals(OrderState.DELIVERED, order.getOrderState());
  }

  @Test(expected = UnsupportedStateTransitionException.class)
  public void shouldRejectDeliveredWhenNotPickedUp() {
    order.noteDelivered();
  }

  @Test
  public void shouldScheduleCourier() {
    Courier courier = new Courier(new PersonName("John", "Doe"),
            new Address("1 Main St", null, "Oakland", "CA", "94612"));
    order.schedule(courier);
    assertEquals(courier, order.getAssignedCourier());
  }

  @Test
  public void shouldFollowFullHappyPath() {
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
