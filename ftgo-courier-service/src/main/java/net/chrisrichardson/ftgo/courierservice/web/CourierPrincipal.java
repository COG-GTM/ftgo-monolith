package net.chrisrichardson.ftgo.courierservice.web;

public final class CourierPrincipal {

  private final boolean operator;
  private final long courierId;

  private CourierPrincipal(boolean operator, long courierId) {
    this.operator = operator;
    this.courierId = courierId;
  }

  public static CourierPrincipal operator() {
    return new CourierPrincipal(true, 0);
  }

  public static CourierPrincipal courier(long id) {
    return new CourierPrincipal(false, id);
  }

  public boolean isOperator() {
    return operator;
  }

  public boolean canManage(long courierId) {
    return operator || this.courierId == courierId;
  }
}
