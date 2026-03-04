package com.ftgo.security;

import com.ftgo.security.util.SecurityUtils;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Shared test application for ftgo-security-lib integration tests.
 *
 * <p>Scans the {@code com.ftgo.security} package to pick up all
 * security configuration classes, handlers, and utilities.
 *
 * <p>Provides test endpoints for verifying:
 * <ul>
 *   <li>Basic authentication ({@code /api/test})</li>
 *   <li>User context propagation ({@code /api/test/user-context})</li>
 *   <li>Authorities/roles ({@code /api/test/authorities})</li>
 * </ul>
 */
@SpringBootApplication
public class TestApplication {

    @RestController
    static class TestController {

        @GetMapping("/api/test")
        public String test() {
            return "OK";
        }

        /**
         * Returns current user context from SecurityContextHolder.
         * Used by JWT integration tests to verify user context propagation.
         */
        @GetMapping("/api/test/user-context")
        public Map<String, Object> userContext() {
            Map<String, Object> context = new LinkedHashMap<>();
            context.put("authenticated", SecurityUtils.isAuthenticated());
            context.put("userId", SecurityUtils.getCurrentUserId().orElse(null));
            context.put("username", SecurityUtils.getCurrentUsername().orElse(null));
            context.put("roles", SecurityUtils.getCurrentRoles());
            return context;
        }

        /**
         * Returns current authorities from SecurityContextHolder.
         * Used by JWT integration tests to verify role-based authorities.
         */
        @GetMapping("/api/test/authorities")
        public Map<String, Object> authorities() {
            Map<String, Object> result = new LinkedHashMap<>();
            Collection<String> authorities = SecurityContextHolder.getContext().getAuthentication()
                    .getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
            result.put("authorities", authorities);
            return result;
        }
    }
}
