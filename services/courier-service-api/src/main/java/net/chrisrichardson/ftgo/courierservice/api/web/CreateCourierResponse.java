package net.chrisrichardson.ftgo.courierservice.api.web;

/**
 * Response DTO for courier creation.
 *
 * <p>Service ownership: Courier Service</p>
 */
public class CreateCourierResponse {

  private long courierId;

  private CreateCourierResponse() {
  }

  public CreateCourierResponse(long courierId) {
    this.courierId = courierId;
  }

  public long getCourierId() {
    return courierId;
  }

  public void setCourierId(long courierId) {
    this.courierId = courierId;
  }
}
