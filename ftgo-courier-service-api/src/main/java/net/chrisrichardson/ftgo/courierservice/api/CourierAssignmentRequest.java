package net.chrisrichardson.ftgo.courierservice.api;

import java.time.LocalDateTime;

public class CourierAssignmentRequest {

  private long orderId;
  private LocalDateTime readyBy;
  private Double restaurantLatitude;
  private Double restaurantLongitude;

  public CourierAssignmentRequest() {
  }

  public CourierAssignmentRequest(long orderId, LocalDateTime readyBy, Double restaurantLatitude, Double restaurantLongitude) {
    this.orderId = orderId;
    this.readyBy = readyBy;
    this.restaurantLatitude = restaurantLatitude;
    this.restaurantLongitude = restaurantLongitude;
  }

  public long getOrderId() {
    return orderId;
  }

  public void setOrderId(long orderId) {
    this.orderId = orderId;
  }

  public LocalDateTime getReadyBy() {
    return readyBy;
  }

  public void setReadyBy(LocalDateTime readyBy) {
    this.readyBy = readyBy;
  }

  public Double getRestaurantLatitude() {
    return restaurantLatitude;
  }

  public void setRestaurantLatitude(Double restaurantLatitude) {
    this.restaurantLatitude = restaurantLatitude;
  }

  public Double getRestaurantLongitude() {
    return restaurantLongitude;
  }

  public void setRestaurantLongitude(Double restaurantLongitude) {
    this.restaurantLongitude = restaurantLongitude;
  }
}
