package net.chrisrichardson.ftgo.security.util;

import org.junit.After;
import org.junit.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link SecurityUtils}.
 */
public class SecurityUtilsTest {

    @After
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void shouldReturnEmptyWhenNoAuthentication() {
        SecurityContextHolder.clearContext();

        Optional<String> username = SecurityUtils.getCurrentUsername();
        assertFalse(username.isPresent());
        assertFalse(SecurityUtils.isAuthenticated());
    }

    @Test
    public void shouldReturnUsernameWhenAuthenticated() {
        Authentication auth = new TestingAuthenticationToken("testuser", "password", "ROLE_USER");
        SecurityContextHolder.getContext().setAuthentication(auth);

        Optional<String> username = SecurityUtils.getCurrentUsername();
        assertTrue(username.isPresent());
        assertEquals("testuser", username.get());
    }

    @Test
    public void shouldReturnTrueWhenAuthenticated() {
        Authentication auth = new TestingAuthenticationToken("testuser", "password", "ROLE_USER");
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertTrue(SecurityUtils.isAuthenticated());
    }

    @Test
    public void shouldReturnFalseForAnonymousAuthentication() {
        List<GrantedAuthority> authorities = Arrays.<GrantedAuthority>asList(
                new SimpleGrantedAuthority("ROLE_ANONYMOUS")
        );
        Authentication auth = new AnonymousAuthenticationToken("key", "anonymous", authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertFalse(SecurityUtils.isAuthenticated());
        assertFalse(SecurityUtils.getCurrentUsername().isPresent());
    }

    @Test
    public void shouldCheckRoleWithPrefix() {
        Authentication auth = new TestingAuthenticationToken("testuser", "password", "ROLE_ADMIN");
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertTrue(SecurityUtils.hasRole("ADMIN"));
        assertTrue(SecurityUtils.hasRole("ROLE_ADMIN"));
        assertFalse(SecurityUtils.hasRole("USER"));
    }

    @Test
    public void shouldCheckAuthority() {
        Authentication auth = new TestingAuthenticationToken("testuser", "password", "ROLE_USER", "READ_ORDERS");
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertTrue(SecurityUtils.hasAuthority("ROLE_USER"));
        assertTrue(SecurityUtils.hasAuthority("READ_ORDERS"));
        assertFalse(SecurityUtils.hasAuthority("WRITE_ORDERS"));
    }

    @Test
    public void shouldReturnAuthorities() {
        Authentication auth = new TestingAuthenticationToken("testuser", "password", "ROLE_USER", "ROLE_ADMIN");
        SecurityContextHolder.getContext().setAuthentication(auth);

        Collection<? extends GrantedAuthority> authorities = SecurityUtils.getAuthorities();
        assertEquals(2, authorities.size());
    }

    @Test
    public void shouldReturnRolesWithoutPrefix() {
        Authentication auth = new TestingAuthenticationToken("testuser", "password", "ROLE_USER", "ROLE_ADMIN");
        SecurityContextHolder.getContext().setAuthentication(auth);

        Collection<String> roles = SecurityUtils.getRoles();
        assertEquals(2, roles.size());
        assertTrue(roles.contains("USER"));
        assertTrue(roles.contains("ADMIN"));
    }

    @Test
    public void shouldReturnEmptyAuthoritiesWhenNotAuthenticated() {
        SecurityContextHolder.clearContext();

        Collection<? extends GrantedAuthority> authorities = SecurityUtils.getAuthorities();
        assertTrue(authorities.isEmpty());
    }

    @Test
    public void shouldReturnCurrentAuthentication() {
        Authentication auth = new TestingAuthenticationToken("testuser", "password", "ROLE_USER");
        SecurityContextHolder.getContext().setAuthentication(auth);

        Optional<Authentication> result = SecurityUtils.getCurrentAuthentication();
        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getName());
    }

    @Test
    public void shouldReturnEmptyRolesWhenNotAuthenticated() {
        SecurityContextHolder.clearContext();

        Collection<String> roles = SecurityUtils.getRoles();
        assertTrue(roles.isEmpty());
    }
}
