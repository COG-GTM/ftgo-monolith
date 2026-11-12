package net.chrisrichardson.ftgo.orderservice.web;

import net.chrisrichardson.ftgo.common.security.FtgoUser;
import net.chrisrichardson.ftgo.domain.Order;

public class OrderAuthorization {

  public static boolean canView(FtgoUser caller, Order order) {
    return caller.isAdmin()
            || caller.isConsumer(order.getConsumerId())
            || order.getRestaurant() != null && caller.isRestaurant(order.getRestaurant().getId())
            || order.getAssignedCourier() != null && caller.isCourier(order.getAssignedCourier().getId());
  }

  public static boolean canActAsConsumer(FtgoUser caller, Order order) {
    return caller.isAdmin() || caller.isConsumer(order.getConsumerId());
  }

  public static boolean canActAsRestaurant(FtgoUser caller, Order order) {
    return caller.isAdmin()
            || order.getRestaurant() != null && caller.isRestaurant(order.getRestaurant().getId());
  }

  public static boolean canActAsCourier(FtgoUser caller, Order order) {
    return caller.isAdmin()
            || order.getAssignedCourier() != null && caller.isCourier(order.getAssignedCourier().getId());
  }

  private OrderAuthorization() {
  }
}
