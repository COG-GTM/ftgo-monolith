package com.ftgo.template.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Service-specific security configuration for the template microservice.
 * <p>
 * Imports the shared FTGO security auto-configuration which provides:
 * <ul>
 *   <li>Stateless session management (no HTTP sessions)</li>
 *   <li>CSRF disabled for REST APIs</li>
 *   <li>CORS configured for API gateway</li>
 *   <li>Public actuator health/info endpoints</li>
 *   <li>All other endpoints require authentication</li>
 *   <li>JSON error responses for 401/403</li>
 *   <li>JWT-based authentication when {@code ftgo.security.jwt.enabled=true}</li>
 *   <li>Role-based authorization with role hierarchy (EM-37)</li>
 *   <li>Method-level security ({@code @PreAuthorize}, {@code @Secured}, JSR-250)</li>
 *   <li>Custom permission evaluator for resource ownership validation</li>
 * </ul>
 * </p>
 *
 * <h3>Enabling JWT Authentication</h3>
 * <p>
 * Add the following to the service's {@code application.properties}:
 * <pre>
 * ftgo.security.jwt.enabled=true
 * ftgo.security.jwt.secret=${JWT_SECRET}
 * ftgo.security.jwt.issuer=ftgo-platform
 * ftgo.security.jwt.access-token-expiration=PT15M
 * ftgo.security.jwt.refresh-token-expiration=P7D
 * </pre>
 * The JWT secret <strong>must</strong> be injected via an environment variable
 * ({@code JWT_SECRET}) — never hardcode it.
 * </p>
 *
 * <h3>Accessing User Context</h3>
 * <p>
 * Once JWT is enabled, the authenticated user context is available via
 * {@link com.ftgo.security.util.SecurityUtils}:
 * <pre>
 * Optional&lt;Long&gt; userId = SecurityUtils.getCurrentUserId();
 * Optional&lt;String&gt; username = SecurityUtils.getCurrentUsername();
 * List&lt;String&gt; roles = SecurityUtils.getCurrentRoles();
 * boolean canCreate = SecurityUtils.hasPermission("order:create");
 * </pre>
 * </p>
 *
 * <h3>Role-Based Authorization (EM-37)</h3>
 * <p>
 * The RBAC framework provides four roles:
 * {@link com.ftgo.security.authorization.FtgoRole#CUSTOMER CUSTOMER},
 * {@link com.ftgo.security.authorization.FtgoRole#RESTAURANT_OWNER RESTAURANT_OWNER},
 * {@link com.ftgo.security.authorization.FtgoRole#COURIER COURIER}, and
 * {@link com.ftgo.security.authorization.FtgoRole#ADMIN ADMIN}.
 * </p>
 * <p>
 * Use {@code @PreAuthorize} annotations on service methods:
 * <pre>
 * // Admin only
 * &#64;PreAuthorize("hasRole('ADMIN')")
 * public void adminOperation() { }
 *
 * // Customer owns the resource
 * &#64;PreAuthorize("hasRole('CUSTOMER') and #consumerId == authentication.principal.userId")
 * public Order createOrder(Long consumerId, ...) { }
 *
 * // Permission + ownership check
 * &#64;PreAuthorize("hasPermission(#orderId, 'Order', 'VIEW')")
 * public Order getOrder(Long orderId) { }
 * </pre>
 * </p>
 * <p>
 * To implement resource ownership validation, create a
 * {@link com.ftgo.security.authorization.ResourceOwnershipStrategy} bean
 * for each resource type. These are auto-discovered and registered.
 * </p>
 *
 * <p>
 * To customize security for a specific service, add additional
 * {@link org.springframework.security.web.SecurityFilterChain} beans here
 * or extend the public paths via {@code ftgo.security.public-paths} in
 * application.properties.
 * </p>
 */
@Configuration
@Import(com.ftgo.security.config.FtgoSecurityAutoConfiguration.class)
public class ServiceSecurityConfiguration {
    // Service-specific security customizations can be added here.
    // The base configuration from ftgo-security-lib handles defaults.
    // JWT authentication is automatically activated when
    // ftgo.security.jwt.enabled=true via JwtAutoConfiguration.
    //
    // RBAC (EM-37) is automatically enabled via FtgoSecurityAutoConfiguration:
    //   - Role hierarchy: ADMIN > RESTAURANT_OWNER > CUSTOMER, ADMIN > COURIER
    //   - Method security: @PreAuthorize, @Secured, @RolesAllowed
    //   - Permission evaluator: hasPermission() with ownership strategies
    //
    // To add resource ownership validation, define a bean:
    //   @Bean
    //   public ResourceOwnershipStrategy orderOwnershipStrategy(OrderRepository repo) {
    //       return new ResourceOwnershipStrategy() {
    //           public boolean isOwner(Long userId, Serializable resourceId) {
    //               return repo.findById((Long) resourceId)
    //                   .map(o -> o.getConsumerId().equals(userId)).orElse(false);
    //           }
    //           public String getResourceType() { return "Order"; }
    //       };
    //   }
}
