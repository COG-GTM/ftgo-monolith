package com.ftgo.courier.security;

/**
 * Security authorization rules for the Courier Service.
 *
 * <p>Method-level security is enabled via {@code @EnableMethodSecurity} in the shared
 * {@code FtgoBaseSecurityConfig}. This class documents the authorization rules that
 * must be applied when controllers are implemented.
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
 * {@code hasPermission()} with the {@code FtgoPermissionEvaluator}
 * from {@code shared/ftgo-security-lib}.
 *
 * <h3>Required {@code @PreAuthorize} Annotations</h3>
 * <pre>
 * // Track delivery — CUSTOMER (own), COURIER (assigned), or ADMIN
 * &#064;PreAuthorize("hasRole('ADMIN') or hasRole('COURIER') "
 *     + "or (hasRole('CUSTOMER') and hasPermission(#id, 'Delivery', 'TRACK'))")
 * &#064;GetMapping("/deliveries/{id}/track")
 * public DeliveryStatus trackDelivery(&#064;PathVariable String id) { ... }
 *
 * // Update delivery status — COURIER (assigned) or ADMIN
 * &#064;PreAuthorize("hasRole('ADMIN') or (hasRole('COURIER') "
 *     + "and hasPermission(#id, 'Delivery', 'UPDATE_STATUS'))")
 * &#064;PutMapping("/deliveries/{id}/status")
 * public void updateDeliveryStatus(&#064;PathVariable String id, ...) { ... }
 *
 * // View courier's assigned orders — COURIER (own) or ADMIN
 * &#064;PreAuthorize("hasRole('ADMIN') or (hasRole('COURIER') and hasPermission(#id, 'Courier', 'VIEW'))")
 * &#064;GetMapping("/couriers/{id}/orders")
 * public List&lt;Order&gt; getCourierOrders(&#064;PathVariable String id) { ... }
 * </pre>
 */
public final class CourierServiceSecurityConfig {
    private CourierServiceSecurityConfig() {
        // Documentation-only class — not instantiable
    }
}
