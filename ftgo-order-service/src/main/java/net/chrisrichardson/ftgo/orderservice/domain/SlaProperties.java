package net.chrisrichardson.ftgo.orderservice.domain;

import net.chrisrichardson.ftgo.domain.OrderState;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Delivery SLA thresholds, in minutes, for orders sitting in a non-terminal state.
 * Configured with {@code ftgo.sla.default-threshold-minutes} and
 * {@code ftgo.sla.state-threshold-minutes.<STATE>}.
 */
@ConfigurationProperties(prefix = "ftgo.sla")
public class SlaProperties {

  public static final int DEFAULT_THRESHOLD_MINUTES = 30;

  private int defaultThresholdMinutes = DEFAULT_THRESHOLD_MINUTES;

  private Map<OrderState, Integer> stateThresholdMinutes = new EnumMap<>(OrderState.class);

  public int getDefaultThresholdMinutes() {
    return defaultThresholdMinutes;
  }

  public void setDefaultThresholdMinutes(int defaultThresholdMinutes) {
    this.defaultThresholdMinutes = defaultThresholdMinutes;
  }

  public Map<OrderState, Integer> getStateThresholdMinutes() {
    return stateThresholdMinutes;
  }

  public void setStateThresholdMinutes(Map<OrderState, Integer> stateThresholdMinutes) {
    Map<OrderState, Integer> copy = new EnumMap<>(OrderState.class);
    if (stateThresholdMinutes != null) {
      copy.putAll(stateThresholdMinutes);
    }
    this.stateThresholdMinutes = copy;
  }

  public int thresholdMinutesFor(OrderState orderState) {
    Integer override = stateThresholdMinutes.get(orderState);
    return override == null ? defaultThresholdMinutes : override;
  }

  public Map<String, Integer> effectiveThresholdMinutes() {
    Map<String, Integer> thresholds = new LinkedHashMap<>();
    for (OrderState state : OrderState.nonTerminalStates()) {
      thresholds.put(state.name(), thresholdMinutesFor(state));
    }
    return thresholds;
  }
}
