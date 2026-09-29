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
import java.util.EnumSet;
import java.util.function.Consumer;

import static org.junit.Assert.*;

public class OrderTest {

  private Restaurant restaurant;
  private Order order;

  @Before
  public void setUp() {
    restaurant = new Restaurant("Ajanta",
            new Address("1 Main St", null, "Oakland", "CA", "94612"),
            new RestaurantMenu(Collections.emptyList()));
    order = new Order(1L, restaurant, Arrays.asList(
            new OrderLineItem("1", "Chicken Vindaloo", new Money("12.34"), 2),
            new OrderLineItem("2", "Samosa", new Money("5.00"), 3)));
  }

  @Test
  public void shouldCreateOrderInApprovedState() {
    assertEquals(OrderState.APPROVED, order.getOrderState());
    assertEquals(Long.valueOf(1L), order.getConsumerId());
    assertSame(restaurant, order.getRestaurant());
    assertEquals(2, order.getLineItems().size());
  }

  @Test
  public void shouldCalculateOrderTotal() {
    assertEquals(new Money("39.68"), order.getOrderTotal());
  }

  @Test
  public void shouldCancelApprovedOrder() {
    order.cancel();

    assertEquals(OrderState.CANCELLED, order.getOrderState());
  }

  @Test
  public void shouldRejectCancelWhenNotApproved() {
    assertRejectedInAllStatesExcept(OrderState.APPROVED, Order::cancel);
  }

  @Test
  public void shouldAcceptTicket() {
    LocalDateTime readyBy = LocalDateTime.now().plusHours(1);

    order.acceptTicket(readyBy);

    assertEquals(OrderState.ACCEPTED, order.getOrderState());
    assertEquals(readyBy, order.getReadyBy());
  }

  @Test
  public void shouldRejectAcceptTicketWhenNotApproved() {
    assertRejectedInAllStatesExcept(OrderState.APPROVED,
            o -> o.acceptTicket(LocalDateTime.now().plusHours(1)));
  }

  @Test
  public void shouldTransitionPreparingOnlyFromAccepted() {
    Order accepted = orderInState(OrderState.ACCEPTED);
    accepted.notePreparing();
    assertEquals(OrderState.PREPARING, accepted.getOrderState());

    assertRejectedInAllStatesExcept(OrderState.ACCEPTED, Order::notePreparing);
  }

  @Test
  public void shouldTransitionReadyOnlyFromPreparing() {
    Order preparing = orderInState(OrderState.PREPARING);
    preparing.noteReadyForPickup();
    assertEquals(OrderState.READY_FOR_PICKUP, preparing.getOrderState());

    assertRejectedInAllStatesExcept(OrderState.PREPARING, Order::noteReadyForPickup);
  }

  @Test
  public void shouldTransitionPickedUpOnlyFromReady() {
    Order ready = orderInState(OrderState.READY_FOR_PICKUP);
    ready.notePickedUp();
    assertEquals(OrderState.PICKED_UP, ready.getOrderState());

    assertRejectedInAllStatesExcept(OrderState.READY_FOR_PICKUP, Order::notePickedUp);
  }

  @Test
  public void shouldTransitionDeliveredOnlyFromPickedUp() {
    Order pickedUp = orderInState(OrderState.PICKED_UP);
    pickedUp.noteDelivered();
    assertEquals(OrderState.DELIVERED, pickedUp.getOrderState());

    assertRejectedInAllStatesExcept(OrderState.PICKED_UP, Order::noteDelivered);
  }

  @Test
  public void shouldScheduleCourier() {
    assertNull(order.getAssignedCourier());
    Courier courier = new Courier(new PersonName("Jane", "Doe"),
            new Address("2 Main St", null, "Oakland", "CA", "94612"));

    order.schedule(courier);

    assertSame(courier, order.getAssignedCourier());
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

  private void assertRejectedInAllStatesExcept(OrderState allowed, Consumer<Order> transition) {
    for (OrderState state : EnumSet.complementOf(EnumSet.of(allowed))) {
      Order o = orderInState(state);
      try {
        transition.accept(o);
        fail("Expected UnsupportedStateTransitionException in state " + state);
      } catch (UnsupportedStateTransitionException e) {
        assertEquals(state, o.getOrderState());
      }
    }
  }

  private Order orderInState(OrderState target) {
    Order o = new Order(1L, restaurant, Collections.emptyList());
    if (target == OrderState.CANCELLED) {
      o.cancel();
      return o;
    }
    if (target == OrderState.APPROVED) return o;
    o.acceptTicket(LocalDateTime.now().plusHours(1));
    if (target == OrderState.ACCEPTED) return o;
    o.notePreparing();
    if (target == OrderState.PREPARING) return o;
    o.noteReadyForPickup();
    if (target == OrderState.READY_FOR_PICKUP) return o;
    o.notePickedUp();
    if (target == OrderState.PICKED_UP) return o;
    o.noteDelivered();
    return o;
  }
}
