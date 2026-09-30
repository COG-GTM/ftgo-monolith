package net.chrisrichardson.ftgo.orderservice.security;

import net.chrisrichardson.ftgo.domain.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Object-level authorization for orders. Operations staff may act on any order; a consumer
 * may only act on orders that belong to the consumer id bound to their authenticated identity.
 */
public class OrderAccessPolicy {

  private static final String OPERATIONS_AUTHORITY = "ROLE_" + FtgoRoles.OPERATIONS;

  public boolean canAccess(Order order) {
    return order.getConsumerId() != null && canActForConsumer(order.getConsumerId());
  }

  public boolean canActForConsumer(long consumerId) {
    return isOperations() || callerConsumerId().filter(id -> id == consumerId).isPresent();
  }

  public boolean isOperations() {
    return authenticatedCaller()
            .map(auth -> auth.getAuthorities().stream().anyMatch(a -> OPERATIONS_AUTHORITY.equals(a.getAuthority())))
            .orElse(false);
  }

  public Optional<Long> callerConsumerId() {
    return authenticatedCaller()
            .map(Authentication::getPrincipal)
            .filter(FtgoUser.class::isInstance)
            .flatMap(principal -> ((FtgoUser) principal).getConsumerId());
  }

  private Optional<Authentication> authenticatedCaller() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()
            || !(authentication.getPrincipal() instanceof FtgoUser)) {
      return Optional.empty();
    }
    return Optional.of(authentication);
  }
}
