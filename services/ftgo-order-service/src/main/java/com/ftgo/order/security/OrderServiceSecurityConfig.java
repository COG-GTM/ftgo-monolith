package com.ftgo.order.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Security configuration for the Order Service.
 *
 * <p>Enables method-level security and defines the authorization rules
 * for order-related endpoints using {@code @PreAuthorize} annotations.
 *
 * <h3>Permission Matrix</h3>
 * <table>
 *   <tr><th>Endpoint</th><th>CUSTOMER</th><th>RESTAURANT_OWNER</th><th>COURIER</th><th>ADMIN</th></tr>
 *   <tr><td>POST /orders</td><td>Yes</td><td>Yes*</td><td>No</td><td>Yes</td></tr>
 *   <tr><td>GET /orders/{id}</td><td>Own</td><td>Related</td><td>Assigned</td><td>Yes</td></tr>
 *   <tr><td>GET /orders</td><td>Own</td><td>Related</td><td>Assigned</td><td>Yes</td></tr>
 *   <tr><td>POST /orders/{id}/cancel</td><td>Own</td><td>No</td><td>No</td><td>Yes</td></tr>
 * </table>
 * <p>* RESTAURANT_OWNER inherits CUSTOMER permissions via role hierarchy.
 *
 * <p>Resource ownership is enforced using {@code hasPermission()} with the
 * {@link com.ftgo.security.authorization.FtgoPermissionEvaluator}.
 */
@Configuration
@EnableMethodSecurity
public class OrderServiceSecurityConfig {
    // Method-level security is enabled via @EnableMethodSecurity.
    // Authorization rules are applied directly on controller methods
    // using @PreAuthorize annotations.
    //
    // Supported expressions:
    //   hasRole('CUSTOMER')          — role check with hierarchy support
    //   hasRole('RESTAURANT_OWNER')  — restaurant owner access
    //   hasRole('COURIER')           — courier access
    //   hasRole('ADMIN')             — admin access
    //   hasPermission(#id, 'Order', 'VIEW')   — ownership check
    //   hasPermission(#id, 'Order', 'CANCEL') — ownership check for cancel
    //
    // Example controller usage:
    //   @PreAuthorize("hasRole('CUSTOMER')")
    //   @PostMapping("/orders")
    //   public Order createOrder(...) { ... }
    //
    //   @PreAuthorize("hasRole('ADMIN') or hasRole('COURIER') or hasRole('RESTAURANT_OWNER') "
    //       + "or (hasRole('CUSTOMER') and hasPermission(#id, 'Order', 'VIEW'))")
    //   @GetMapping("/orders/{id}")
    //   public Order getOrder(@PathVariable String id) { ... }
    //
    //   @PreAuthorize("hasRole('ADMIN') or (hasRole('CUSTOMER') and hasPermission(#id, 'Order', 'CANCEL'))")
    //   @PostMapping("/orders/{id}/cancel")
    //   public Order cancelOrder(@PathVariable String id) { ... }
}
