package com.ftgo.consumer.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Security configuration for the Consumer Service.
 *
 * <p>Enables method-level security and defines the authorization rules
 * for consumer-related endpoints using {@code @PreAuthorize} annotations.
 *
 * <h3>Permission Matrix</h3>
 * <table>
 *   <tr><th>Endpoint</th><th>CUSTOMER</th><th>RESTAURANT_OWNER</th><th>COURIER</th><th>ADMIN</th></tr>
 *   <tr><td>POST /consumers</td><td>Yes</td><td>Yes*</td><td>No</td><td>Yes</td></tr>
 *   <tr><td>GET /consumers/{id}</td><td>Own</td><td>Own*</td><td>No</td><td>Yes</td></tr>
 *   <tr><td>GET /consumers</td><td>No</td><td>No</td><td>No</td><td>Yes</td></tr>
 * </table>
 * <p>* RESTAURANT_OWNER inherits CUSTOMER permissions via role hierarchy.
 *
 * <p>Resource ownership is enforced using {@code hasPermission()} with the
 * {@link com.ftgo.security.authorization.FtgoPermissionEvaluator}.
 */
@Configuration
@EnableMethodSecurity
public class ConsumerServiceSecurityConfig {
    // Method-level security is enabled via @EnableMethodSecurity.
    // Authorization rules are applied directly on controller methods
    // using @PreAuthorize annotations.
    //
    // Supported expressions:
    //   hasRole('CUSTOMER')          — role check with hierarchy support
    //   hasRole('ADMIN')             — admin access
    //   hasPermission(#id, 'Consumer', 'VIEW') — ownership check
    //
    // Example controller usage:
    //   @PreAuthorize("hasRole('CUSTOMER')")
    //   @PostMapping("/consumers")
    //   public Consumer createConsumer(...) { ... }
    //
    //   @PreAuthorize("hasRole('ADMIN') or (hasRole('CUSTOMER') and hasPermission(#id, 'Consumer', 'VIEW'))")
    //   @GetMapping("/consumers/{id}")
    //   public Consumer getConsumer(@PathVariable String id) { ... }
}
