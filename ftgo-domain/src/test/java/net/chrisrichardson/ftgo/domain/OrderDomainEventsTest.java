package net.chrisrichardson.ftgo.domain;

import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.common.UnsupportedStateTransitionException;
import net.chrisrichardson.ftgo.domain.events.*;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class OrderDomainEventsTest {

  private static final long ORDER_ID = 101L;
  private static final long CONSUMER_ID = 7L;
  private static final long RESTAURANT_ID = 3L;

  private Order order;

  @Before
  public void setUp() {
    Restaurant restaurant = new Restaurant(RESTAURANT_ID, "Ajanta", new RestaurantMenu(Collections.emptyList()));
    order = new Order(CONSUMER_ID, restaurant,
            Collections.singletonList(new OrderLineItem("1", "Chicken Vindaloo", new Money("12.34"), 2)));
    order.setId(ORDER_ID);
  }

  @Test
  public void shouldNotRecordEventsOnConstruction() {
    assertTrue(order.pullDomainEvents().isEmpty());
  }

  @Test
  public void shouldRecordOrderCreatedEvent() {
    order.noteCreated();

    OrderCreatedEvent event = singleEvent(OrderCreatedEvent.class);
    assertEquals(Long.valueOf(ORDER_ID), event.getOrderId());
    assertEquals(Long.valueOf(CONSUMER_ID), event.getConsumerId());
    assertEquals(Long.valueOf(RESTAURANT_ID), event.getRestaurantId());
    assertEquals(OrderState.APPROVED, event.getOrderState());
    assertEquals(new Money("24.68"), event.getOrderTotal());
    assertNotNull(event.getOccurredAt());
  }

  @Test(expected = IllegalStateException.class)
  public void shouldRejectNoteCreatedBeforeOrderIsPersisted() {
    order.setId(null);
    order.noteCreated();
  }

  @Test
  public void shouldRecordOrderCancelledEvent() {
    order.cancel();

    assertTransition(singleEvent(OrderCancelledEvent.class), OrderState.APPROVED, OrderState.CANCELLED);
  }

  @Test
  public void shouldRecordOrderAcceptedEvent() {
    LocalDateTime readyBy = LocalDateTime.now().plusHours(1);

    order.acceptTicket(readyBy);

    OrderAcceptedEvent event = singleEvent(OrderAcceptedEvent.class);
    assertTransition(event, OrderState.APPROVED, OrderState.ACCEPTED);
    assertEquals(readyBy, event.getReadyBy());
  }

  @Test
  public void shouldRecordEventForEachTransitionOfHappyPath() {
    order.acceptTicket(LocalDateTime.now().plusHours(1));
    order.notePreparing();
    order.noteReadyForPickup();
    order.notePickedUp();
    order.noteDelivered();

    List<OrderDomainEvent> events = order.pullDomainEvents();

    assertEquals(5, events.size());
    assertTransition(events.get(0), OrderAcceptedEvent.class, OrderState.APPROVED, OrderState.ACCEPTED);
    assertTransition(events.get(1), OrderPreparationStartedEvent.class, OrderState.ACCEPTED, OrderState.PREPARING);
    assertTransition(events.get(2), OrderReadyForPickupEvent.class, OrderState.PREPARING, OrderState.READY_FOR_PICKUP);
    assertTransition(events.get(3), OrderPickedUpEvent.class, OrderState.READY_FOR_PICKUP, OrderState.PICKED_UP);
    assertTransition(events.get(4), OrderDeliveredEvent.class, OrderState.PICKED_UP, OrderState.DELIVERED);
  }

  @Test
  public void shouldClearEventsWhenPulled() {
    order.cancel();

    assertEquals(1, order.pullDomainEvents().size());
    assertTrue(order.pullDomainEvents().isEmpty());
  }

  @Test
  public void shouldNotRecordEventWhenTransitionIsRejected() {
    try {
      order.noteDelivered();
      fail("expected UnsupportedStateTransitionException");
    } catch (UnsupportedStateTransitionException e) {
      // expected
    }

    assertTrue(order.pullDomainEvents().isEmpty());
  }

  @Test
  public void shouldNotRecordEventWhenAcceptIsRejected() {
    try {
      order.acceptTicket(LocalDateTime.now().minusMinutes(1));
      fail("expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // expected
    }

    assertTrue(order.pullDomainEvents().isEmpty());
  }

  private <E extends OrderDomainEvent> E singleEvent(Class<E> type) {
    List<OrderDomainEvent> events = order.pullDomainEvents();
    assertEquals(1, events.size());
    assertTrue(type.isInstance(events.get(0)));
    return type.cast(events.get(0));
  }

  private void assertTransition(OrderDomainEvent event, Class<? extends OrderStateTransitionEvent> type,
                                OrderState from, OrderState to) {
    assertTrue("expected " + type.getSimpleName() + " but was " + event, type.isInstance(event));
    assertTransition((OrderStateTransitionEvent) event, from, to);
  }

  private void assertTransition(OrderStateTransitionEvent event, OrderState from, OrderState to) {
    assertEquals(Long.valueOf(ORDER_ID), event.getOrderId());
    assertEquals(from, event.getPreviousState());
    assertEquals(to, event.getOrderState());
  }
}
