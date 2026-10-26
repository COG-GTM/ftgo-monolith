package net.chrisrichardson.ftgo.courierservice.web.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class CourierUserDetails extends User {

  private final Long courierId;

  public CourierUserDetails(String username, String password, Long courierId,
                            Collection<? extends GrantedAuthority> authorities) {
    super(username, password, authorities);
    this.courierId = courierId;
  }

  public Long getCourierId() {
    return courierId;
  }
}
