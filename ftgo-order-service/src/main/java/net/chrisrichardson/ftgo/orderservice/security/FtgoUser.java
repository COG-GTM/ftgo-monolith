package net.chrisrichardson.ftgo.orderservice.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.Optional;

public class FtgoUser extends User {

  private final Long consumerId;

  public FtgoUser(String username, String password, Collection<? extends GrantedAuthority> authorities, Long consumerId) {
    super(username, password, authorities);
    this.consumerId = consumerId;
  }

  public Optional<Long> getConsumerId() {
    return Optional.ofNullable(consumerId);
  }
}
