package net.chrisrichardson.ftgo.orderservice.domain;

import io.micrometer.core.instrument.MeterRegistry;
import net.chrisrichardson.ftgo.common.events.DomainEventSubscriber;
import net.chrisrichardson.ftgo.domain.events.OrderDomainEvent;

import java.util.Optional;

/**
 * Counts order lifecycle events as {@code order_state_transitions{state=...}}.
 * The tag values are bounded by the {@link net.chrisrichardson.ftgo.domain.OrderState} enum.
 */
public class OrderStateTransitionMetrics implements DomainEventSubscriber<OrderDomainEvent> {

  public static final String METRIC_NAME = "order_state_transitions";

  private final Optional<MeterRegistry> meterRegistry;

  public OrderStateTransitionMetrics(Optional<MeterRegistry> meterRegistry) {
    this.meterRegistry = meterRegistry;
  }

  @Override
  public Class<OrderDomainEvent> getEventType() {
    return OrderDomainEvent.class;
  }

  @Override
  public void handle(OrderDomainEvent event) {
    meterRegistry.ifPresent(mr -> mr.counter(METRIC_NAME, "state", event.getOrderState().name()).increment());
  }
}
