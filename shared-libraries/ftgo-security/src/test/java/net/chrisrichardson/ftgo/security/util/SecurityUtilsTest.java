package net.chrisrichardson.ftgo.security.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link SecurityUtils}.
 */
class SecurityUtilsTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUsernameReturnsEmptyWhenNotAuthenticated() {
        Optional<String> username = SecurityUtils.getCurrentUsername();
        assertFalse(username.isPresent());
    }

    @Test
    void getCurrentUsernameReturnsNameWhenAuthenticated() {
        setAuthentication("testuser", "ROLE_USER");

        Optional<String> username = SecurityUtils.getCurrentUsername();
        assertTrue(username.isPresent());
        assertEquals("testuser", username.get());
    }

    @Test
    void isAuthenticatedReturnsFalseWhenNoAuth() {
        assertFalse(SecurityUtils.isAuthenticated());
    }

    @Test
    void isAuthenticatedReturnsTrueWhenAuthenticated() {
        setAuthentication("testuser", "ROLE_USER");
        assertTrue(SecurityUtils.isAuthenticated());
    }

    @Test
    void hasAuthorityReturnsTrueForGrantedAuthority() {
        setAuthentication("testuser", "ROLE_ADMIN");
        assertTrue(SecurityUtils.hasAuthority("ROLE_ADMIN"));
    }

    @Test
    void hasAuthorityReturnsFalseForMissingAuthority() {
        setAuthentication("testuser", "ROLE_USER");
        assertFalse(SecurityUtils.hasAuthority("ROLE_ADMIN"));
    }

    @Test
    void getCurrentAuthoritiesReturnsEmptyWhenNotAuthenticated() {
        assertTrue(SecurityUtils.getCurrentAuthorities().isEmpty());
    }

    @Test
    void getCurrentAuthoritiesReturnsAuthoritiesWhenAuthenticated() {
        setAuthentication("testuser", "ROLE_USER");
        assertFalse(SecurityUtils.getCurrentAuthorities().isEmpty());
        assertEquals(1, SecurityUtils.getCurrentAuthorities().size());
    }

    @Test
    void getCurrentAuthenticationReturnsEmptyWhenNoContext() {
        assertFalse(SecurityUtils.getCurrentAuthentication().isPresent());
    }

    private void setAuthentication(String username, String... roles) {
        List<SimpleGrantedAuthority> authorities = List.of(roles).stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(username, "password", authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
