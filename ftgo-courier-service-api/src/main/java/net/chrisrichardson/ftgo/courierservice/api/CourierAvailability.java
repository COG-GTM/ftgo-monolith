package net.chrisrichardson.ftgo.courierservice.api;

public class CourierAvailability {

  private Boolean available;

  public CourierAvailability() {
  }

  public CourierAvailability(Boolean available) {
    this.available = available;
  }

  public Boolean isAvailable() {
    return available;
  }

  public void setAvailable(Boolean available) {
    this.available = available;
  }
}
