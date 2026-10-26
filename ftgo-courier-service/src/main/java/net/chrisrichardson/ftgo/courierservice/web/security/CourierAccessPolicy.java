package net.chrisrichardson.ftgo.courierservice.web.security;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public class CourierAccessPolicy {

  static final String DISPATCHER_AUTHORITY = "ROLE_DISPATCHER";

  /**
   * A dispatcher may act on any courier, a courier only on its own record.
   */
  public boolean canAccessCourier(long courierId) {
    return canAccessCourier(currentAuthentication(), courierId);
  }

  boolean canAccessCourier(Authentication authentication, long courierId) {
    if (!isAuthenticated(authentication))
      return false;
    if (hasAuthority(authentication, DISPATCHER_AUTHORITY))
      return true;
    Object principal = authentication.getPrincipal();
    if (!(principal instanceof CourierUserDetails))
      return false;
    Long ownCourierId = ((CourierUserDetails) principal).getCourierId();
    return ownCourierId != null && ownCourierId == courierId;
  }

  private Authentication currentAuthentication() {
    return SecurityContextHolder.getContext().getAuthentication();
  }

  private boolean isAuthenticated(Authentication authentication) {
    return authentication != null && authentication.isAuthenticated()
            && !(authentication instanceof AnonymousAuthenticationToken);
  }

  private boolean hasAuthority(Authentication authentication, String authority) {
    if (!isAuthenticated(authentication))
      return false;
    for (GrantedAuthority granted : authentication.getAuthorities()) {
      if (authority.equals(granted.getAuthority()))
        return true;
    }
    return false;
  }
}
