package net.chrisrichardson.ftgo.orderservice.domain;

import net.chrisrichardson.ftgo.domain.Order;
import net.chrisrichardson.ftgo.domain.OrderRepository;
import net.chrisrichardson.ftgo.domain.OrderState;
import net.chrisrichardson.ftgo.orderservice.api.web.SlaBreachReport;
import net.chrisrichardson.ftgo.orderservice.api.web.SlaBreachedOrder;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Finds orders that have been sitting in a non-terminal state longer than the configured SLA.
 */
@Transactional(readOnly = true)
public class SlaBreachService {

  private final OrderRepository orderRepository;
  private final SlaProperties slaProperties;
  private final Clock clock;

  public SlaBreachService(OrderRepository orderRepository, SlaProperties slaProperties, Clock clock) {
    this.orderRepository = orderRepository;
    this.slaProperties = slaProperties;
    this.clock = clock;
  }

  public SlaBreachReport findBreaches() {
    return findBreaches(OrderState.nonTerminalStates());
  }

  public SlaBreachReport findBreaches(OrderState orderState) {
    if (orderState.isTerminal()) {
      throw new TerminalOrderStateException(orderState);
    }
    return findBreaches(Collections.singletonList(orderState));
  }

  private SlaBreachReport findBreaches(List<OrderState> states) {
    LocalDateTime now = LocalDateTime.now(clock);

    Map<String, Integer> counts = new LinkedHashMap<>();
    Map<String, Integer> thresholds = new LinkedHashMap<>();
    for (OrderState state : states) {
      counts.put(state.name(), 0);
      thresholds.put(state.name(), slaProperties.thresholdMinutesFor(state));
    }

    List<SlaBreachedOrder> breaches = new ArrayList<>();
    for (Order order : orderRepository.findAllByOrderStateIn(states)) {
      OrderState state = order.getOrderState();
      LocalDateTime stateEnteredAt = order.getStateEnteredTime();
      if (stateEnteredAt == null) {
        continue;
      }
      int thresholdMinutes = slaProperties.thresholdMinutesFor(state);
      long ageMinutes = Duration.between(stateEnteredAt, now).toMinutes();
      if (ageMinutes <= thresholdMinutes) {
        continue;
      }
      breaches.add(new SlaBreachedOrder(order.getId(),
              state.name(),
              order.getRestaurant() == null ? null : order.getRestaurant().getName(),
              stateEnteredAt,
              ageMinutes,
              thresholdMinutes));
      counts.merge(state.name(), 1, Integer::sum);
    }

    breaches.sort((a, b) -> Long.compare(b.getAgeMinutes(), a.getAgeMinutes()));

    return new SlaBreachReport(counts, thresholds, breaches);
  }
}
