package net.chrisrichardson.ftgo.domain;

import net.chrisrichardson.ftgo.common.UnsupportedStateTransitionException;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class OrderTransitionTest {
  private record Transition(OrderState from, OrderState to, String timestamp, Consumer<Order> apply) {
  }

  @Test
  void transitionsKeepStateTimestampAndInvalidStateExceptions() throws Exception {
    List<Transition> transitions = List.of(
            new Transition(OrderState.ACCEPTED, OrderState.PREPARING, "preparingTime", Order::notePreparing),
            new Transition(OrderState.PREPARING, OrderState.READY_FOR_PICKUP, "readyForPickupTime", Order::noteReadyForPickup),
            new Transition(OrderState.READY_FOR_PICKUP, OrderState.PICKED_UP, "pickedUpTime", Order::notePickedUp),
            new Transition(OrderState.PICKED_UP, OrderState.DELIVERED, "deliveredTime", Order::noteDelivered));
    Field state = Order.class.getDeclaredField("orderState");
    state.setAccessible(true);
    for (Transition transition : transitions) {
      Field timestamp = Order.class.getDeclaredField(transition.timestamp());
      timestamp.setAccessible(true);
      for (OrderState initial : OrderState.values()) {
        Order order = new Order(1, null, List.of());
        state.set(order, initial);
        if (initial == transition.from()) {
          LocalDateTime before = LocalDateTime.now();
          transition.apply().accept(order);
          assertEquals(transition.to(), order.getOrderState());
          assertFalse(((LocalDateTime) timestamp.get(order)).isBefore(before));
        } else {
          var error = assertThrows(UnsupportedStateTransitionException.class, () -> transition.apply().accept(order));
          assertEquals("current state: " + initial, error.getMessage());
          assertEquals(initial, order.getOrderState());
          assertNull(timestamp.get(order));
        }
      }
      Order order = new Order(1, null, List.of());
      state.set(order, null);
      assertThrows(NullPointerException.class, () -> transition.apply().accept(order));
      assertNull(timestamp.get(order));
    }
  }
}
