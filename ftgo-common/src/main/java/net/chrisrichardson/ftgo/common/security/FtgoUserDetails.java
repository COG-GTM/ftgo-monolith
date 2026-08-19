package net.chrisrichardson.ftgo.common.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class FtgoUserDetails implements UserDetails {

  private final String username;
  private final String password;
  private final boolean enabled;
  private final UserRole role;
  private final Long subjectId;

  public FtgoUserDetails(String username, String password, boolean enabled, UserRole role, Long subjectId) {
    this.username = username;
    this.password = password;
    this.enabled = enabled;
    this.role = role;
    this.subjectId = subjectId;
  }

  public static FtgoUserDetails of(AppUser user) {
    return new FtgoUserDetails(user.getUsername(), user.getPasswordHash(), user.isEnabled(), user.getRole(), user.getSubjectId());
  }

  public UserRole getRole() {
    return role;
  }

  /**
   * The id of the domain entity this account acts for, or null for accounts -
   * such as administrators - that are not tied to a single entity.
   */
  public Long getSubjectId() {
    return subjectId;
  }

  public boolean hasRole(UserRole expected) {
    return role == expected;
  }

  public boolean isSubject(Long id) {
    return subjectId != null && subjectId.equals(id);
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
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
    return enabled;
  }
}
