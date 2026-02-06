package net.chrisrichardson.ftgo.courierservice.api;

public class AssignCourierResponse {
  private Long courierId;

  public AssignCourierResponse() {
  }

  public AssignCourierResponse(Long courierId) {
    this.courierId = courierId;
  }

  public Long getCourierId() {
    return courierId;
  }

  public void setCourierId(Long courierId) {
    this.courierId = courierId;
  }
}
