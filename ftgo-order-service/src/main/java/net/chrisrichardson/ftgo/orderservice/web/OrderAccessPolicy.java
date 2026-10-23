package net.chrisrichardson.ftgo.orderservice.web;

import net.chrisrichardson.ftgo.common.security.FtgoPrincipal;
import net.chrisrichardson.ftgo.common.security.FtgoRole;
import net.chrisrichardson.ftgo.domain.Order;

/**
 * Who may act on an order: consumers only on their own orders, restaurants only on orders
 * placed with them, couriers only on orders assigned to them, administrators on any order.
 */
public class OrderAccessPolicy {

  public boolean canCreateOrderFor(FtgoPrincipal principal, long consumerId) {
    return isAdmin(principal) || actsAs(principal, FtgoRole.CONSUMER, consumerId);
  }

  public boolean canActAsConsumer(FtgoPrincipal principal, Order order) {
    return isAdmin(principal) || actsAs(principal, FtgoRole.CONSUMER, order.getConsumerId());
  }

  public boolean canActAsRestaurant(FtgoPrincipal principal, Order order) {
    return isAdmin(principal)
            || (order.getRestaurant() != null && actsAs(principal, FtgoRole.RESTAURANT, order.getRestaurant().getId()));
  }

  public boolean canActAsCourier(FtgoPrincipal principal, Order order) {
    return isAdmin(principal)
            || (order.getAssignedCourier() != null && actsAs(principal, FtgoRole.COURIER, order.getAssignedCourier().getId()));
  }

  private boolean isAdmin(FtgoPrincipal principal) {
    return principal != null && principal.isAdmin();
  }

  private boolean actsAs(FtgoPrincipal principal, FtgoRole role, Long actorId) {
    return principal != null && principal.actsAs(role, actorId);
  }
}
