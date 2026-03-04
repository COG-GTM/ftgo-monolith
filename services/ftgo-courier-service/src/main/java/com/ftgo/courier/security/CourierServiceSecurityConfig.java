package com.ftgo.courier.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Security configuration for the Courier Service.
 *
 * <p>Enables method-level security and defines the authorization rules
 * for courier/delivery-related endpoints using {@code @PreAuthorize} annotations.
 *
 * <h3>Permission Matrix</h3>
 * <table>
 *   <tr><th>Endpoint</th><th>CUSTOMER</th><th>RESTAURANT_OWNER</th><th>COURIER</th><th>ADMIN</th></tr>
 *   <tr><td>GET /deliveries/{id}/track</td><td>Own</td><td>No</td><td>Assigned</td><td>Yes</td></tr>
 *   <tr><td>PUT /deliveries/{id}/status</td><td>No</td><td>No</td><td>Assigned</td><td>Yes</td></tr>
 *   <tr><td>GET /couriers/{id}/orders</td><td>No</td><td>No</td><td>Own</td><td>Yes</td></tr>
 * </table>
 *
 * <p>Resource ownership and assignment checks are enforced using
 * {@code hasPermission()} with the
 * {@link com.ftgo.security.authorization.FtgoPermissionEvaluator}.
 */
@Configuration
@EnableMethodSecurity
public class CourierServiceSecurityConfig {
    // Method-level security is enabled via @EnableMethodSecurity.
    // Authorization rules are applied directly on controller methods
    // using @PreAuthorize annotations.
    //
    // Supported expressions:
    //   hasRole('COURIER')           — courier access
    //   hasRole('CUSTOMER')          — customer access (for delivery tracking)
    //   hasRole('ADMIN')             — admin access
    //   hasPermission(#id, 'Delivery', 'TRACK')          — ownership check
    //   hasPermission(#id, 'Delivery', 'UPDATE_STATUS')  — assignment check
    //
    // Example controller usage:
    //   @PreAuthorize("hasRole('ADMIN') or hasRole('COURIER') "
    //       + "or (hasRole('CUSTOMER') and hasPermission(#id, 'Delivery', 'TRACK'))")
    //   @GetMapping("/deliveries/{id}/track")
    //   public DeliveryStatus trackDelivery(@PathVariable String id) { ... }
    //
    //   @PreAuthorize("hasRole('ADMIN') or (hasRole('COURIER') "
    //       + "and hasPermission(#id, 'Delivery', 'UPDATE_STATUS'))")
    //   @PutMapping("/deliveries/{id}/status")
    //   public void updateDeliveryStatus(@PathVariable String id, ...) { ... }
}
