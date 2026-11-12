package net.chrisrichardson.ftgo.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;

public class FtgoUserDetails extends org.springframework.security.core.userdetails.User {

  private final FtgoRole role;
  private final Long consumerId;

  public FtgoUserDetails(String username, String passwordHash, FtgoRole role, Long consumerId) {
    super(username, passwordHash, true, true, true, true,
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name())));
    this.role = role;
    this.consumerId = consumerId;
  }

  public FtgoRole getRole() {
    return role;
  }

  public Long getConsumerId() {
    return consumerId;
  }
}
