package net.chrisrichardson.ftgo.courierservice.web;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

@Component
public class CourierAccessPolicy {

  public void checkCanRead(Authentication authentication, long courierId) {
    if (isOperator(authentication)) {
      return;
    }

    Object principal = authentication.getPrincipal();
    if (principal instanceof CourierUser
            && ((CourierUser) principal).getCourierId() != null
            && ((CourierUser) principal).getCourierId() == courierId) {
      return;
    }
    throw new AccessDeniedException("Access denied");
  }

  public void checkIsOperator(Authentication authentication) {
    if (!isOperator(authentication)) {
      throw new AccessDeniedException("Access denied");
    }
  }

  private boolean isOperator(Authentication authentication) {
    return authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch("ROLE_OPERATOR"::equals);
  }
}
