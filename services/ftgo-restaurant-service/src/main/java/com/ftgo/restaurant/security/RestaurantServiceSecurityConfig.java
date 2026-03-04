package com.ftgo.restaurant.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Security configuration for the Restaurant Service.
 *
 * <p>Enables method-level security and defines the authorization rules
 * for restaurant-related endpoints using {@code @PreAuthorize} annotations.
 *
 * <h3>Permission Matrix</h3>
 * <table>
 *   <tr><th>Endpoint</th><th>CUSTOMER</th><th>RESTAURANT_OWNER</th><th>COURIER</th><th>ADMIN</th></tr>
 *   <tr><td>GET /restaurants</td><td>Yes</td><td>Yes</td><td>No</td><td>Yes</td></tr>
 *   <tr><td>GET /restaurants/{id}</td><td>Yes</td><td>Yes</td><td>No</td><td>Yes</td></tr>
 *   <tr><td>POST /restaurants</td><td>No</td><td>Yes</td><td>No</td><td>Yes</td></tr>
 *   <tr><td>PUT /restaurants/{id}</td><td>No</td><td>Own</td><td>No</td><td>Yes</td></tr>
 *   <tr><td>DELETE /restaurants/{id}</td><td>No</td><td>Own</td><td>No</td><td>Yes</td></tr>
 * </table>
 *
 * <p>Resource ownership for RESTAURANT_OWNER is enforced using
 * {@code hasPermission()} with the
 * {@link com.ftgo.security.authorization.FtgoPermissionEvaluator}.
 */
@Configuration
@EnableMethodSecurity
public class RestaurantServiceSecurityConfig {
    // Method-level security is enabled via @EnableMethodSecurity.
    // Authorization rules are applied directly on controller methods
    // using @PreAuthorize annotations.
    //
    // Supported expressions:
    //   hasRole('CUSTOMER')          — role check with hierarchy support
    //   hasRole('RESTAURANT_OWNER')  — restaurant owner access
    //   hasRole('ADMIN')             — admin access
    //   hasPermission(#id, 'Restaurant', 'UPDATE') — ownership check
    //   hasPermission(#id, 'Restaurant', 'DELETE') — ownership check
    //
    // Example controller usage:
    //   @PreAuthorize("hasRole('CUSTOMER')")
    //   @GetMapping("/restaurants")
    //   public List<Restaurant> listRestaurants() { ... }
    //
    //   @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    //   @PostMapping("/restaurants")
    //   public Restaurant createRestaurant(...) { ... }
    //
    //   @PreAuthorize("hasRole('ADMIN') or (hasRole('RESTAURANT_OWNER') "
    //       + "and hasPermission(#id, 'Restaurant', 'UPDATE'))")
    //   @PutMapping("/restaurants/{id}")
    //   public Restaurant updateRestaurant(@PathVariable String id, ...) { ... }
    //
    //   @PreAuthorize("hasRole('ADMIN') or (hasRole('RESTAURANT_OWNER') "
    //       + "and hasPermission(#id, 'Restaurant', 'DELETE'))")
    //   @DeleteMapping("/restaurants/{id}")
    //   public void deleteRestaurant(@PathVariable String id) { ... }
}
