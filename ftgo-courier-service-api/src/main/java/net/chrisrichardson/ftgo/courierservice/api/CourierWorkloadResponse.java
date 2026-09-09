package net.chrisrichardson.ftgo.courierservice.api;

public class CourierWorkloadResponse {

  private long courierId;
  private int activeDeliveries;
  private boolean available;

  public CourierWorkloadResponse() {
  }

  public CourierWorkloadResponse(long courierId, int activeDeliveries, boolean available) {
    this.courierId = courierId;
    this.activeDeliveries = activeDeliveries;
    this.available = available;
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
}
