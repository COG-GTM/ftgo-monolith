package net.chrisrichardson.ftgo.orderservice.api.web;

import java.time.LocalDateTime;

public class SlaBreachedOrder {

  private Long orderId;
  private String state;
  private String restaurantName;
  private LocalDateTime stateEnteredAt;
  private long ageMinutes;
  private int thresholdMinutes;
  private long minutesOverSla;

  private SlaBreachedOrder() {
  }

  public SlaBreachedOrder(Long orderId, String state, String restaurantName, LocalDateTime stateEnteredAt,
                          long ageMinutes, int thresholdMinutes) {
    this.orderId = orderId;
    this.state = state;
    this.restaurantName = restaurantName;
    this.stateEnteredAt = stateEnteredAt;
    this.ageMinutes = ageMinutes;
    this.thresholdMinutes = thresholdMinutes;
    this.minutesOverSla = ageMinutes - thresholdMinutes;
  }

  public Long getOrderId() {
    return orderId;
  }

  public String getState() {
    return state;
  }

  public String getRestaurantName() {
    return restaurantName;
  }

  public LocalDateTime getStateEnteredAt() {
    return stateEnteredAt;
  }

  public long getAgeMinutes() {
    return ageMinutes;
  }

  public int getThresholdMinutes() {
    return thresholdMinutes;
  }

  public long getMinutesOverSla() {
    return minutesOverSla;
  }
}
