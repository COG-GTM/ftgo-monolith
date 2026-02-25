package com.ftgo.security.jwt;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Immutable {@link UserDetails} implementation backed by JWT claims.
 * <p>
 * Carries the user ID, roles, and service-specific permissions extracted
 * from the access token. Instances of this class are placed in the
 * {@link org.springframework.security.core.context.SecurityContextHolder}
 * so that downstream service code can access user context via
 * {@link com.ftgo.security.util.SecurityUtils}.
 * </p>
 */
public class JwtUserDetails implements UserDetails {

    private final Long userId;
    private final String username;
    private final List<String> roles;
    private final List<String> permissions;

    public JwtUserDetails(Long userId, String username,
                          List<String> roles, List<String> permissions) {
        this.userId = userId;
        this.username = username;
        this.roles = roles != null ? List.copyOf(roles) : Collections.emptyList();
        this.permissions = permissions != null ? List.copyOf(permissions) : Collections.emptyList();
    }

    /** Returns the numeric user identifier from the JWT {@code userId} claim. */
    public Long getUserId() {
        return userId;
    }

    /** Returns the roles from the JWT {@code roles} claim. */
    public List<String> getRoles() {
        return roles;
    }

    /** Returns the service-specific permissions from the JWT {@code permissions} claim. */
    public List<String> getPermissions() {
        return permissions;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public String getPassword() {
        // JWT-based auth does not use passwords
        return "";
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
}
