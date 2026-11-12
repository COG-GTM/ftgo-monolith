package net.chrisrichardson.ftgo.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class FtgoUser implements UserDetails {

  private final String username;
  private final String passwordHash;
  private final Role role;
  private final Long entityId;

  public FtgoUser(String username, String passwordHash, Role role, Long entityId) {
    this.username = username;
    this.passwordHash = passwordHash;
    this.role = role;
    this.entityId = entityId;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return Collections.singleton(new SimpleGrantedAuthority("ROLE_" + role.name()));
  }

  @Override
  public String getPassword() {
    return passwordHash;
  }

  @Override
  public String getUsername() {
    return username;
  }

  public Role getRole() {
    return role;
  }

  public Long getEntityId() {
    return entityId;
  }

  public boolean isOperator() {
    return role == Role.OPERATOR;
  }

  public boolean isConsumer(long id) {
    return role == Role.CONSUMER && entityId != null && entityId == id;
  }

  public boolean isRestaurant(long id) {
    return role == Role.RESTAURANT && entityId != null && entityId == id;
  }

  public boolean isCourier(long id) {
    return role == Role.COURIER && entityId != null && entityId == id;
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
}
