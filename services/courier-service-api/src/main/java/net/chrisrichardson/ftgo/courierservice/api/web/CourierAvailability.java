package net.chrisrichardson.ftgo.courierservice.api.web;

/**
 * DTO for updating courier availability status.
 *
 * <p>Service ownership: Courier Service</p>
 */
public class CourierAvailability {

  private boolean available;

  private CourierAvailability() {
  }

  public CourierAvailability(boolean available) {
    this.available = available;
  }

  public boolean isAvailable() {
    return available;
  }

  public void setAvailable(boolean available) {
    this.available = available;
  }
}
