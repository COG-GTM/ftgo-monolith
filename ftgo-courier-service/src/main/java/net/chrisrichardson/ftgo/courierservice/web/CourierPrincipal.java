package net.chrisrichardson.ftgo.courierservice.web;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

/**
 * Authenticated user of the courier API. Courier accounts are bound to a single courier id.
 */
public class CourierPrincipal extends User {

  private final Long courierId;

  public CourierPrincipal(String username, String password, Collection<? extends GrantedAuthority> authorities, Long courierId) {
    super(username, password, authorities);
    this.courierId = courierId;
  }

  public Long getCourierId() {
    return courierId;
  }
}
