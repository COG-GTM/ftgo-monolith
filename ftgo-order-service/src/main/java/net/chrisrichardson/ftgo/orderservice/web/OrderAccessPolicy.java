package net.chrisrichardson.ftgo.orderservice.web;

import net.chrisrichardson.ftgo.domain.Order;
import net.chrisrichardson.ftgo.security.FtgoUser;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
public class OrderAccessPolicy {

  public void checkCanRead(FtgoUser caller, Order order) {
    if (caller.isOperator()
            || caller.isConsumer(order.getConsumerId())
            || caller.isRestaurant(order.getRestaurant().getId())
            || (order.getAssignedCourier() != null && caller.isCourier(order.getAssignedCourier().getId()))) {
      return;
    }
    throw new AccessDeniedException("Caller cannot read this order");
  }

  public void checkIsConsumer(FtgoUser caller, Order order) {
    if (!caller.isOperator() && !caller.isConsumer(order.getConsumerId())) {
      throw new AccessDeniedException("Only the order's consumer can perform this action");
    }
  }

  public void checkIsRestaurant(FtgoUser caller, Order order) {
    if (!caller.isOperator() && !caller.isRestaurant(order.getRestaurant().getId())) {
      throw new AccessDeniedException("Only the order's restaurant can perform this action");
    }
  }

  public void checkIsAssignedCourier(FtgoUser caller, Order order) {
    if (!caller.isOperator()
            && (order.getAssignedCourier() == null || !caller.isCourier(order.getAssignedCourier().getId()))) {
      throw new AccessDeniedException("Only the assigned courier can perform this action");
    }
  }
}
