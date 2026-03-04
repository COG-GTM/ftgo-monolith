package com.ftgo.restaurant.security;

/**
 * Security authorization rules for the Restaurant Service.
 *
 * <p>Method-level security is enabled via {@code @EnableMethodSecurity} in the shared
 * {@code FtgoBaseSecurityConfig}. This class documents the authorization rules that
 * must be applied when controllers are implemented.
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
 * {@code hasPermission()} with the {@code FtgoPermissionEvaluator}
 * from {@code shared/ftgo-security-lib}.
 *
 * <h3>Required {@code @PreAuthorize} Annotations</h3>
 * <pre>
 * // List restaurants — CUSTOMER or above
 * &#064;PreAuthorize("hasRole('CUSTOMER')")
 * &#064;GetMapping("/restaurants")
 * public List&lt;Restaurant&gt; listRestaurants() { ... }
 *
 * // Create restaurant — RESTAURANT_OWNER or ADMIN
 * &#064;PreAuthorize("hasRole('RESTAURANT_OWNER')")
 * &#064;PostMapping("/restaurants")
 * public Restaurant createRestaurant(...) { ... }
 *
 * // Update restaurant — RESTAURANT_OWNER (own) or ADMIN
 * &#064;PreAuthorize("hasRole('ADMIN') or (hasRole('RESTAURANT_OWNER') "
 *     + "and hasPermission(#id, 'Restaurant', 'UPDATE'))")
 * &#064;PutMapping("/restaurants/{id}")
 * public Restaurant updateRestaurant(&#064;PathVariable String id, ...) { ... }
 *
 * // Delete restaurant — RESTAURANT_OWNER (own) or ADMIN
 * &#064;PreAuthorize("hasRole('ADMIN') or (hasRole('RESTAURANT_OWNER') "
 *     + "and hasPermission(#id, 'Restaurant', 'DELETE'))")
 * &#064;DeleteMapping("/restaurants/{id}")
 * public void deleteRestaurant(&#064;PathVariable String id) { ... }
 * </pre>
 */
public final class RestaurantServiceSecurityConfig {
    private RestaurantServiceSecurityConfig() {
        // Documentation-only class — not instantiable
    }
}
