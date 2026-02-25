package com.ftgo.security.authorization;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Enables Spring method-level security with support for:
 * <ul>
 *   <li>{@code @PreAuthorize} / {@code @PostAuthorize} annotations</li>
 *   <li>{@code @Secured} annotations</li>
 *   <li>JSR-250 annotations ({@code @RolesAllowed})</li>
 *   <li>Custom {@link FtgoPermissionEvaluator} for ownership checks</li>
 *   <li>Role hierarchy integration</li>
 * </ul>
 *
 * <h3>Usage Examples</h3>
 * <pre>
 * // Role-based access
 * &#64;PreAuthorize("hasRole('ADMIN')")
 * public void adminOnlyMethod() { }
 *
 * // Ownership check
 * &#64;PreAuthorize("hasRole('CUSTOMER') and #consumerId == authentication.principal.userId")
 * public Order createOrder(Long consumerId, ...) { }
 *
 * // Permission-based access with custom evaluator
 * &#64;PreAuthorize("hasPermission(#orderId, 'Order', 'VIEW')")
 * public Order getOrder(Long orderId) { }
 * </pre>
 *
 * @see FtgoPermissionEvaluator
 * @see FtgoRoleHierarchyConfiguration
 */
@Configuration
@EnableMethodSecurity(
        prePostEnabled = true,
        securedEnabled = true,
        jsr250Enabled = true
)
public class FtgoMethodSecurityConfiguration {

    @Autowired(required = false)
    private FtgoPermissionEvaluator ftgoPermissionEvaluator;

    @Autowired(required = false)
    private RoleHierarchy roleHierarchy;

    /**
     * Creates a custom {@link MethodSecurityExpressionHandler} that integrates:
     * <ul>
     *   <li>The FTGO permission evaluator for {@code hasPermission()} expressions</li>
     *   <li>The role hierarchy for role-based expressions</li>
     * </ul>
     *
     * @return configured expression handler
     */
    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler() {
        DefaultMethodSecurityExpressionHandler handler =
                new DefaultMethodSecurityExpressionHandler();

        if (ftgoPermissionEvaluator != null) {
            handler.setPermissionEvaluator(ftgoPermissionEvaluator);
        }

        if (roleHierarchy != null) {
            handler.setRoleHierarchy(roleHierarchy);
        }

        return handler;
    }
}
