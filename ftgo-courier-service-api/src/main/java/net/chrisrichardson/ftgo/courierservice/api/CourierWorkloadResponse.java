package net.chrisrichardson.ftgo.courierservice.api;

import java.time.LocalDateTime;

public class CourierWorkloadResponse {

  private long courierId;
  private int activeDeliveries;
  private boolean available;
  private LocalDateTime lastLocationUpdate;

  public CourierWorkloadResponse() {
  }

  public CourierWorkloadResponse(long courierId, int activeDeliveries, boolean available,
                                 LocalDateTime lastLocationUpdate) {
    this.courierId = courierId;
    this.activeDeliveries = activeDeliveries;
    this.available = available;
    this.lastLocationUpdate = lastLocationUpdate;
  }

  public long getCourierId() {
    return courierId;
  }

  public void setCourierId(long courierId) {
    this.courierId = courierId;
  }

  public int getActiveDeliveries() {
    return activeDeliveries;
  }

  public void setActiveDeliveries(int activeDeliveries) {
    this.activeDeliveries = activeDeliveries;
  }

  public boolean isAvailable() {
    return available;
  }

  public void setAvailable(boolean available) {
    this.available = available;
  }

  public LocalDateTime getLastLocationUpdate() {
    return lastLocationUpdate;
  }

  public void setLastLocationUpdate(LocalDateTime lastLocationUpdate) {
    this.lastLocationUpdate = lastLocationUpdate;
  }
}
