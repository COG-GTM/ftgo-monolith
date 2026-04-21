package net.chrisrichardson.ftgo.courierservice.api;

import java.time.LocalDateTime;

public class CourierWorkloadResponse {

  private long courierId;
  private int activeDeliveries;
  private boolean available;
  private Double currentLatitude;
  private Double currentLongitude;
  private LocalDateTime lastLocationUpdate;

  public CourierWorkloadResponse() {
  }

  public CourierWorkloadResponse(long courierId, int activeDeliveries, boolean available,
                                 Double currentLatitude, Double currentLongitude,
                                 LocalDateTime lastLocationUpdate) {
    this.courierId = courierId;
    this.activeDeliveries = activeDeliveries;
    this.available = available;
    this.currentLatitude = currentLatitude;
    this.currentLongitude = currentLongitude;
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

  public Double getCurrentLatitude() {
    return currentLatitude;
  }

  public void setCurrentLatitude(Double currentLatitude) {
    this.currentLatitude = currentLatitude;
  }

  public Double getCurrentLongitude() {
    return currentLongitude;
  }

  public void setCurrentLongitude(Double currentLongitude) {
    this.currentLongitude = currentLongitude;
  }

  public LocalDateTime getLastLocationUpdate() {
    return lastLocationUpdate;
  }

  public void setLastLocationUpdate(LocalDateTime lastLocationUpdate) {
    this.lastLocationUpdate = lastLocationUpdate;
  }
}
