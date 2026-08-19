package net.chrisrichardson.ftgo.orderservice.domain;

import net.chrisrichardson.ftgo.common.security.CurrentUser;
import net.chrisrichardson.ftgo.common.security.FtgoUserDetails;
import net.chrisrichardson.ftgo.common.security.UserRole;
import net.chrisrichardson.ftgo.domain.Order;
import org.springframework.security.access.AccessDeniedException;

/**
 * Decides what the authenticated caller may do with an order. Consumers are limited
 * to their own orders, restaurants to the orders placed with them and couriers to the
 * orders assigned to them; administrators are unrestricted.
 */
public class OrderAccessPolicy {

  public long consumerIdForNewOrder(long requestedConsumerId) {
    FtgoUserDetails user = CurrentUser.require();
    if (user.hasRole(UserRole.ADMIN))
      return requestedConsumerId;
    if (user.hasRole(UserRole.CONSUMER) && user.getSubjectId() != null) {
      if (requestedConsumerId != 0 && !user.isSubject(requestedConsumerId))
        throw new AccessDeniedException("Cannot place an order for another consumer");
      return user.getSubjectId();
    }
    throw new AccessDeniedException("Not allowed to place orders");
  }

  public long consumerIdForOrderHistory(Long requestedConsumerId) {
    FtgoUserDetails user = CurrentUser.require();
    if (user.hasRole(UserRole.ADMIN)) {
      if (requestedConsumerId == null)
        throw new IllegalArgumentException("consumerId is required");
      return requestedConsumerId;
    }
    if (user.hasRole(UserRole.CONSUMER) && user.getSubjectId() != null) {
      if (requestedConsumerId != null && !user.isSubject(requestedConsumerId))
        throw new AccessDeniedException("Cannot read another consumer's orders");
      return user.getSubjectId();
    }
    throw new AccessDeniedException("Not allowed to read order history");
  }

  public boolean canView(Order order) {
    FtgoUserDetails user = CurrentUser.require();
    switch (user.getRole()) {
      case ADMIN:
        return true;
      case CONSUMER:
        return user.isSubject(order.getConsumerId());
      case RESTAURANT:
        return order.getRestaurant() != null && user.isSubject(order.getRestaurant().getId());
      case COURIER:
        return order.getAssignedCourier() != null && user.isSubject(order.getAssignedCourier().getId());
      default:
        return false;
    }
  }

  public void checkCanChangeOrderContents(Order order) {
    FtgoUserDetails user = CurrentUser.require();
    if (user.hasRole(UserRole.ADMIN) || (user.hasRole(UserRole.CONSUMER) && user.isSubject(order.getConsumerId())))
      return;
    throw new AccessDeniedException("Not allowed to modify this order");
  }

  public void checkCanPerformRestaurantTransition(Order order) {
    FtgoUserDetails user = CurrentUser.require();
    if (user.hasRole(UserRole.ADMIN))
      return;
    if (user.hasRole(UserRole.RESTAURANT) && order.getRestaurant() != null && user.isSubject(order.getRestaurant().getId()))
      return;
    throw new AccessDeniedException("Not allowed to change the preparation state of this order");
  }

  public void checkCanPerformCourierTransition(Order order) {
    FtgoUserDetails user = CurrentUser.require();
    if (user.hasRole(UserRole.ADMIN))
      return;
    if (user.hasRole(UserRole.COURIER) && order.getAssignedCourier() != null && user.isSubject(order.getAssignedCourier().getId()))
      return;
    throw new AccessDeniedException("Not allowed to change the delivery state of this order");
  }
}
