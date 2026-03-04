package com.ftgo.consumer.security;

/**
 * Security authorization rules for the Consumer Service.
 *
 * <p>Method-level security is enabled via {@code @EnableMethodSecurity} in the shared
 * {@code FtgoBaseSecurityConfig}. This class documents the authorization rules that
 * must be applied when controllers are implemented.
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
 * {@code FtgoPermissionEvaluator} from {@code shared/ftgo-security-lib}.
 *
 * <h3>Required {@code @PreAuthorize} Annotations</h3>
 * <pre>
 * // Create consumer — requires CUSTOMER role
 * &#064;PreAuthorize("hasRole('CUSTOMER')")
 * &#064;PostMapping("/consumers")
 * public Consumer createConsumer(...) { ... }
 *
 * // View own consumer — requires CUSTOMER + ownership or ADMIN
 * &#064;PreAuthorize("hasRole('ADMIN') or (hasRole('CUSTOMER') and hasPermission(#id, 'Consumer', 'VIEW'))")
 * &#064;GetMapping("/consumers/{id}")
 * public Consumer getConsumer(&#064;PathVariable String id) { ... }
 *
 * // List all consumers — ADMIN only
 * &#064;PreAuthorize("hasRole('ADMIN')")
 * &#064;GetMapping("/consumers")
 * public List&lt;Consumer&gt; listConsumers() { ... }
 * </pre>
 */
public final class ConsumerServiceSecurityConfig {
    private ConsumerServiceSecurityConfig() {
        // Documentation-only class — not instantiable
    }
}
