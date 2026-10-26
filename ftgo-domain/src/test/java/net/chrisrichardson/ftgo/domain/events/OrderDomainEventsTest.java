package net.chrisrichardson.ftgo.domain.events;

import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.common.UnsupportedStateTransitionException;
import net.chrisrichardson.ftgo.domain.Order;
import net.chrisrichardson.ftgo.domain.OrderLineItem;
import net.chrisrichardson.ftgo.domain.OrderRevision;
import net.chrisrichardson.ftgo.domain.OrderState;
import net.chrisrichardson.ftgo.domain.Restaurant;
import net.chrisrichardson.ftgo.domain.RestaurantMenu;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class OrderDomainEventsTest {

  private static final long CONSUMER_ID = 7L;
  private static final long RESTAURANT_ID = 3L;

  private Order order;

  @Before
  public void setUp() {
    Restaurant restaurant = new Restaurant(RESTAURANT_ID, "Ajanta", new RestaurantMenu(Collections.emptyList()));
    List<OrderLineItem> lineItems = Collections.singletonList(
            new OrderLineItem("1", "Chicken Vindaloo", new Money("12.34"), 2));
    order = new Order(CONSUMER_ID, restaurant, lineItems);
  }

  @Test
  public void shouldRecordOrderCreatedEvent() {
    List<OrderDomainEvent> events = order.releaseDomainEvents();

    assertEquals(1, events.size());
    OrderCreatedEvent created = (OrderCreatedEvent) events.get(0);
    assertEquals(CONSUMER_ID, created.getConsumerId());
    assertEquals(Long.valueOf(RESTAURANT_ID), created.getRestaurantId());
    assertEquals(new Money("24.68"), created.getOrderTotal());
    assertEquals(OrderState.APPROVED, created.getState());
  }

  @Test
  public void releaseShouldClearRecordedEvents() {
    order.releaseDomainEvents();

    assertTrue(order.releaseDomainEvents().isEmpty());
  }

  @Test
  public void shouldRecordEventForEachLifecycleTransition() {
    order.releaseDomainEvents();
    LocalDateTime readyBy = LocalDateTime.now().plusHours(1);

    order.acceptTicket(readyBy);
    OrderAcceptedEvent accepted = (OrderAcceptedEvent) single(order.releaseDomainEvents());
    assertTransition(accepted, OrderState.APPROVED, OrderState.ACCEPTED);
    assertEquals(readyBy, accepted.getReadyBy());

    order.notePreparing();
    assertTransition((OrderPreparingEvent) single(order.releaseDomainEvents()), OrderState.ACCEPTED, OrderState.PREPARING);

    order.noteReadyForPickup();
    assertTransition((OrderReadyForPickupEvent) single(order.releaseDomainEvents()), OrderState.PREPARING, OrderState.READY_FOR_PICKUP);

    order.notePickedUp();
    assertTransition((OrderPickedUpEvent) single(order.releaseDomainEvents()), OrderState.READY_FOR_PICKUP, OrderState.PICKED_UP);

    order.noteDelivered();
    assertTransition((OrderDeliveredEvent) single(order.releaseDomainEvents()), OrderState.PICKED_UP, OrderState.DELIVERED);
  }

  @Test
  public void shouldRecordOrderCancelledEvent() {
    order.releaseDomainEvents();

    order.cancel();

    assertTransition((OrderCancelledEvent) single(order.releaseDomainEvents()), OrderState.APPROVED, OrderState.CANCELLED);
  }

  @Test
  public void shouldNotRecordEventWhenTransitionIsRejected() {
    order.releaseDomainEvents();

    try {
      order.notePreparing();
      fail("expected UnsupportedStateTransitionException");
    } catch (UnsupportedStateTransitionException e) {
      // expected
    }

    assertTrue(order.releaseDomainEvents().isEmpty());
    assertEquals(OrderState.APPROVED, order.getOrderState());
  }

  @Test
  public void shouldRecordOrderRevisedEventWhenQuantitiesChange() {
    order.releaseDomainEvents();

    order.revise(new OrderRevision(Optional.empty(), Collections.singletonMap("1", 3)));

    OrderRevisedEvent revised = (OrderRevisedEvent) single(order.releaseDomainEvents());
    assertEquals(new Money("37.02"), revised.getOrderTotal());
  }

  @Test
  public void shouldNotRecordEventForEmptyRevision() {
    order.releaseDomainEvents();

    order.revise(new OrderRevision(Optional.empty(), Collections.emptyMap()));

    assertTrue(order.releaseDomainEvents().isEmpty());
  }

  private static OrderDomainEvent single(List<OrderDomainEvent> events) {
    assertEquals(1, events.size());
    return events.get(0);
  }

  private static void assertTransition(OrderStateChangedEvent event, OrderState from, OrderState to) {
    assertEquals(from, event.getPreviousState());
    assertEquals(to, event.getNewState());
  }
}
