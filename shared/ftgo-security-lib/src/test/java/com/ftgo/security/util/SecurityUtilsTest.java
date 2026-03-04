package com.ftgo.security.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link SecurityUtils}.
 */
class SecurityUtilsTest {

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("getCurrentUsername returns empty when not authenticated")
    void getCurrentUsernameReturnsEmptyWhenNotAuthenticated() {
        assertTrue(SecurityUtils.getCurrentUsername().isEmpty());
    }

    @Test
    @DisplayName("getCurrentUsername returns username when authenticated")
    void getCurrentUsernameReturnsUsernameWhenAuthenticated() {
        setAuthentication("testuser", "ROLE_USER");

        assertEquals("testuser", SecurityUtils.getCurrentUsername().orElse(null));
    }

    @Test
    @DisplayName("getCurrentAuthorities returns empty when not authenticated")
    void getCurrentAuthoritiesReturnsEmptyWhenNotAuthenticated() {
        assertTrue(SecurityUtils.getCurrentAuthorities().isEmpty());
    }

    @Test
    @DisplayName("getCurrentAuthorities returns authorities when authenticated")
    void getCurrentAuthoritiesReturnsAuthoritiesWhenAuthenticated() {
        setAuthentication("testuser", "ROLE_USER", "ROLE_ADMIN");

        Collection<String> authorities = SecurityUtils.getCurrentAuthorities();
        assertEquals(2, authorities.size());
        assertTrue(authorities.contains("ROLE_USER"));
        assertTrue(authorities.contains("ROLE_ADMIN"));
    }

    @Test
    @DisplayName("hasAuthority returns true when user has the authority")
    void hasAuthorityReturnsTrueWhenUserHasAuthority() {
        setAuthentication("testuser", "ROLE_ADMIN");

        assertTrue(SecurityUtils.hasAuthority("ROLE_ADMIN"));
    }

    @Test
    @DisplayName("hasAuthority returns false when user does not have the authority")
    void hasAuthorityReturnsFalseWhenUserDoesNotHaveAuthority() {
        setAuthentication("testuser", "ROLE_USER");

        assertFalse(SecurityUtils.hasAuthority("ROLE_ADMIN"));
    }

    @Test
    @DisplayName("isAuthenticated returns false when not authenticated")
    void isAuthenticatedReturnsFalseWhenNotAuthenticated() {
        assertFalse(SecurityUtils.isAuthenticated());
    }

    @Test
    @DisplayName("isAuthenticated returns true when authenticated")
    void isAuthenticatedReturnsTrueWhenAuthenticated() {
        setAuthentication("testuser", "ROLE_USER");

        assertTrue(SecurityUtils.isAuthenticated());
    }

    private void setAuthentication(String username, String... roles) {
        List<SimpleGrantedAuthority> authorities = java.util.Arrays.stream(roles)
                .map(SimpleGrantedAuthority::new)
                .toList();

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(username, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
