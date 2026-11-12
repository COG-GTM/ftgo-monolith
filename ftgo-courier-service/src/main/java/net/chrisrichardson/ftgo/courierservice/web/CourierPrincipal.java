package net.chrisrichardson.ftgo.courierservice.web;

public class CourierPrincipal {

  private final boolean operator;
  private final Long courierId;

  private CourierPrincipal(boolean operator, Long courierId) {
    this.operator = operator;
    this.courierId = courierId;
  }

  public static CourierPrincipal operator() {
    return new CourierPrincipal(true, null);
  }

  public static CourierPrincipal courier(long courierId) {
    return new CourierPrincipal(false, courierId);
  }

  public boolean isOperator() {
    return operator;
  }

  public Long getCourierId() {
    return courierId;
  }
}
