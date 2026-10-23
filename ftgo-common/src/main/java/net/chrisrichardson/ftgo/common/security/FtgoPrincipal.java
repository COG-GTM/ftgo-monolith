package net.chrisrichardson.ftgo.common.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Authenticated caller. {@code actorId} identifies the domain entity the caller acts as
 * (consumer, restaurant or courier id); it is null for administrators.
 */
public class FtgoPrincipal implements UserDetails {

  private final String username;
  private final String password;
  private final FtgoRole role;
  private final Long actorId;

  public FtgoPrincipal(String username, String password, FtgoRole role, Long actorId) {
    if (role == FtgoRole.ADMIN) {
      if (actorId != null)
        throw new IllegalArgumentException("ADMIN user " + username + " must not have an actorId");
    } else if (actorId == null) {
      throw new IllegalArgumentException(role + " user " + username + " must have an actorId");
    }
    this.username = username;
    this.password = password;
    this.role = role;
    this.actorId = actorId;
  }

  public FtgoRole getRole() {
    return role;
  }

  public Long getActorId() {
    return actorId;
  }

  public boolean isAdmin() {
    return role == FtgoRole.ADMIN;
  }

  public boolean actsAs(FtgoRole expectedRole, Long expectedActorId) {
    return role == expectedRole && expectedActorId != null && expectedActorId.equals(actorId);
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return Collections.singletonList(new SimpleGrantedAuthority(role.authority()));
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public String getUsername() {
    return username;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  @Override
  public String toString() {
    return "FtgoPrincipal{username='" + username + "', role=" + role + ", actorId=" + actorId + '}';
  }
}
