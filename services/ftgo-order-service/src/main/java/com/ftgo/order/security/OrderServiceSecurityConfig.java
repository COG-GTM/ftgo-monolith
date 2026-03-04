package com.ftgo.order.security;

/**
 * Security authorization rules for the Order Service.
 *
 * <p>Method-level security is enabled via {@code @EnableMethodSecurity} in the shared
 * {@code FtgoBaseSecurityConfig}. This class documents the authorization rules that
 * must be applied when controllers are implemented.
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
 * {@code FtgoPermissionEvaluator} from {@code shared/ftgo-security-lib}.
 *
 * <h3>Required {@code @PreAuthorize} Annotations</h3>
 * <pre>
 * // Create order — requires CUSTOMER role
 * &#064;PreAuthorize("hasRole('CUSTOMER')")
 * &#064;PostMapping("/orders")
 * public Order createOrder(...) { ... }
 *
 * // View order — CUSTOMER (own), RESTAURANT_OWNER, COURIER, or ADMIN
 * &#064;PreAuthorize("hasRole('ADMIN') or hasRole('COURIER') or hasRole('RESTAURANT_OWNER') "
 *     + "or (hasRole('CUSTOMER') and hasPermission(#id, 'Order', 'VIEW'))")
 * &#064;GetMapping("/orders/{id}")
 * public Order getOrder(&#064;PathVariable String id) { ... }
 *
 * // Cancel order — CUSTOMER (own) or ADMIN
 * &#064;PreAuthorize("hasRole('ADMIN') or (hasRole('CUSTOMER') and hasPermission(#id, 'Order', 'CANCEL'))")
 * &#064;PostMapping("/orders/{id}/cancel")
 * public Order cancelOrder(&#064;PathVariable String id) { ... }
 * </pre>
 */
public final class OrderServiceSecurityConfig {
    private OrderServiceSecurityConfig() {
        // Documentation-only class — not instantiable
    }
}
