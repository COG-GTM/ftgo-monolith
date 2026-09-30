package net.chrisrichardson.ftgo.orderservice.web.security;

import net.chrisrichardson.ftgo.domain.Order;
import net.chrisrichardson.ftgo.domain.OrderRepository;
import net.chrisrichardson.ftgo.orderservice.domain.OrderNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class OrderAccessPolicy {

  private final OrderRepository orderRepository;

  public OrderAccessPolicy(OrderRepository orderRepository) {
    this.orderRepository = orderRepository;
  }

  public void requireConsumer(long consumerId) {
    FtgoPrincipal principal = requireRole(FtgoRole.CONSUMER);
    if (principal.getId() != consumerId) {
      throw new AccessDeniedException("Consumer " + principal.getId() + " cannot act for consumer " + consumerId);
    }
  }

  public Order requireConsumerOwns(long orderId) {
    FtgoPrincipal principal = requireRole(FtgoRole.CONSUMER);
    Order order = findOrder(orderId);
    if (order.getConsumerId() == null || order.getConsumerId() != principal.getId()) {
      throw new AccessDeniedException("Consumer " + principal.getId() + " does not own order " + orderId);
    }
    return order;
  }

  public Order requireRestaurantOwns(long orderId) {
    FtgoPrincipal principal = requireRole(FtgoRole.RESTAURANT);
    Order order = findOrder(orderId);
    Long restaurantId = order.getRestaurant() == null ? null : order.getRestaurant().getId();
    if (restaurantId == null || restaurantId != principal.getId()) {
      throw new AccessDeniedException("Restaurant " + principal.getId() + " does not own order " + orderId);
    }
    return order;
  }

  public Order requireCourierAssigned(long orderId) {
    FtgoPrincipal principal = requireRole(FtgoRole.COURIER);
    Order order = findOrder(orderId);
    Long courierId = order.getAssignedCourier() == null ? null : order.getAssignedCourier().getId();
    if (courierId == null || courierId != principal.getId()) {
      throw new AccessDeniedException("Courier " + principal.getId() + " is not assigned to order " + orderId);
    }
    return order;
  }

  private FtgoPrincipal requireRole(FtgoRole role) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof FtgoPrincipal)) {
      throw new AccessDeniedException("Authentication required");
    }
    FtgoPrincipal principal = (FtgoPrincipal) authentication.getPrincipal();
    if (principal.getRole() != role) {
      throw new AccessDeniedException("Role " + role + " required");
    }
    return principal;
  }

  private Order findOrder(long orderId) {
    return orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
  }
}
