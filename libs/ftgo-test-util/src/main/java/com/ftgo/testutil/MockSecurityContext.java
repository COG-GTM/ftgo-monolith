package com.ftgo.testutil;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class MockSecurityContext {

    public static final String DEFAULT_USER = "test-user";
    public static final String DEFAULT_ROLE = "ROLE_USER";

    private MockSecurityContext() {
    }

    public static void setAuthenticated(String username, String... roles) {
        List<SimpleGrantedAuthority> authorities = Arrays.stream(roles)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                username, "password", authorities);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

    public static void setDefaultUser() {
        setAuthenticated(DEFAULT_USER, DEFAULT_ROLE);
    }

    public static void setAdmin() {
        setAuthenticated("admin", "ROLE_ADMIN", "ROLE_USER");
    }

    public static void clear() {
        SecurityContextHolder.clearContext();
    }
}
