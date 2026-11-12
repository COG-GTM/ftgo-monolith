package net.chrisrichardson.ftgo.courierservice.web;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class CourierUser extends User {

  private final Long courierId;

  public CourierUser(String username, String password, Collection<? extends GrantedAuthority> authorities,
                     Long courierId) {
    super(username, password, authorities);
    this.courierId = courierId;
  }

  public Long getCourierId() {
    return courierId;
  }
}
