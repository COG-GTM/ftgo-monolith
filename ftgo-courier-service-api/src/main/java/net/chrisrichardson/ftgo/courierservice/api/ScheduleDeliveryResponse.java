package net.chrisrichardson.ftgo.courierservice.api;

public class ScheduleDeliveryResponse {
  private long courierId;

  private ScheduleDeliveryResponse() {
  }

  public ScheduleDeliveryResponse(long courierId) {
    this.courierId = courierId;
  }

  public long getCourierId() {
    return courierId;
  }

  public void setCourierId(long courierId) {
    this.courierId = courierId;
  }
}
