package net.chrisrichardson.ftgo.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityUtilsTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("getCurrentAuthentication returns empty when no authentication set")
    void getCurrentAuthentication_noAuth_returnsEmpty() {
        Optional<Authentication> auth = SecurityUtils.getCurrentAuthentication();
        assertFalse(auth.isPresent());
    }

    @Test
    @DisplayName("getCurrentAuthentication returns authentication when set")
    void getCurrentAuthentication_withAuth_returnsAuth() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "testuser", "password", Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        Optional<Authentication> result = SecurityUtils.getCurrentAuthentication();
        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getName());
    }

    @Test
    @DisplayName("getCurrentUsername returns username when authenticated")
    void getCurrentUsername_withAuth_returnsUsername() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "testuser", "password", Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        Optional<String> username = SecurityUtils.getCurrentUsername();
        assertTrue(username.isPresent());
        assertEquals("testuser", username.get());
    }

    @Test
    @DisplayName("getCurrentUsername returns empty when not authenticated")
    void getCurrentUsername_noAuth_returnsEmpty() {
        Optional<String> username = SecurityUtils.getCurrentUsername();
        assertFalse(username.isPresent());
    }

    @Test
    @DisplayName("isAuthenticated returns true for authenticated user")
    void isAuthenticated_withAuth_returnsTrue() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "testuser", "password", Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertTrue(SecurityUtils.isAuthenticated());
    }

    @Test
    @DisplayName("isAuthenticated returns false when not authenticated")
    void isAuthenticated_noAuth_returnsFalse() {
        assertFalse(SecurityUtils.isAuthenticated());
    }
}
