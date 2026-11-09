package net.chrisrichardson.ftgo.courierservice.web;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

/**
 * Authorization checks referenced from {@code @PreAuthorize} expressions on {@link CourierController}.
 */
@Component("courierAccess")
public class CourierAccess {

  public static final String DISPATCHER = "DISPATCHER";
  public static final String COURIER = "COURIER";

  public boolean isCourier(Authentication authentication, long courierId) {
    if (authentication == null || !authentication.isAuthenticated()
            || !(authentication.getPrincipal() instanceof CourierPrincipal)) {
      return false;
    }
    CourierPrincipal principal = (CourierPrincipal) authentication.getPrincipal();
    boolean hasCourierRole = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(("ROLE_" + COURIER)::equals);
    return hasCourierRole && principal.getCourierId() != null && principal.getCourierId() == courierId;
  }
}
