package net.chrisrichardson.ftgo.orderservice.domain;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import net.chrisrichardson.ftgo.domain.Order;
import net.chrisrichardson.ftgo.domain.OrderState;
import net.chrisrichardson.ftgo.domain.events.OrderCancelledEvent;
import net.chrisrichardson.ftgo.domain.events.OrderCreatedEvent;
import net.chrisrichardson.ftgo.orderservice.OrderDetailsMother;
import net.chrisrichardson.ftgo.orderservice.RestaurantMother;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertEquals;

public class OrderStateTransitionMetricsTest {

  @Test
  public void shouldCountEventsByResultingState() {
    SimpleMeterRegistry registry = new SimpleMeterRegistry();
    OrderStateTransitionMetrics metrics = new OrderStateTransitionMetrics(Optional.of(registry));
    Order order = new Order(OrderDetailsMother.CONSUMER_ID, RestaurantMother.AJANTA_RESTAURANT,
            OrderDetailsMother.chickenVindalooLineItems());
    order.setId(1L);

    metrics.handle(new OrderCreatedEvent(order));
    order.cancel();
    metrics.handle(new OrderCancelledEvent(order, OrderState.APPROVED));

    assertEquals(1.0, registry.counter(OrderStateTransitionMetrics.METRIC_NAME, "state", "APPROVED").count(), 0.0);
    assertEquals(1.0, registry.counter(OrderStateTransitionMetrics.METRIC_NAME, "state", "CANCELLED").count(), 0.0);
  }

  @Test
  public void shouldIgnoreEventsWithoutMeterRegistry() {
    Order order = new Order(OrderDetailsMother.CONSUMER_ID, RestaurantMother.AJANTA_RESTAURANT,
            OrderDetailsMother.chickenVindalooLineItems());
    order.setId(1L);

    new OrderStateTransitionMetrics(Optional.empty()).handle(new OrderCreatedEvent(order));
  }
}
