package net.chrisrichardson.ftgo.authorization.config;

import net.chrisrichardson.ftgo.authorization.evaluator.FtgoPermissionEvaluator;
import net.chrisrichardson.ftgo.authorization.evaluator.ResourceOwnershipResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;

import java.util.Collections;
import java.util.List;

/**
 * Configures method-level security for FTGO microservices.
 *
 * <p>Enables {@code @PreAuthorize} and {@code @PostAuthorize} annotations on
 * service methods, backed by the FTGO role hierarchy and custom permission
 * evaluator.</p>
 *
 * <p>Example usage in a service:</p>
 * <pre>
 * &#64;PreAuthorize("hasRole('ADMIN')")
 * public void deleteUser(Long userId) { ... }
 *
 * &#64;PreAuthorize("hasPermission(null, 'order:create')")
 * public Order createOrder(CreateOrderRequest request) { ... }
 *
 * &#64;PreAuthorize("hasPermission(#orderId, 'order', 'read')")
 * public Order getOrder(Long orderId) { ... }
 * </pre>
 *
 * @see FtgoPermissionEvaluator
 * @see FtgoRoleHierarchyConfig
 */
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class FtgoMethodSecurityConfig extends GlobalMethodSecurityConfiguration {

    @Autowired(required = false)
    private List<ResourceOwnershipResolver> ownershipResolvers;

    /**
     * Creates the FTGO role hierarchy bean.
     *
     * @return the configured role hierarchy
     */
    @Bean
    public RoleHierarchy roleHierarchy() {
        return FtgoRoleHierarchyConfig.createRoleHierarchy();
    }

    /**
     * Creates the FTGO permission evaluator bean.
     *
     * @return the configured permission evaluator
     */
    @Bean
    public PermissionEvaluator permissionEvaluator() {
        List<ResourceOwnershipResolver> resolvers = ownershipResolvers != null
                ? ownershipResolvers
                : Collections.<ResourceOwnershipResolver>emptyList();
        return new FtgoPermissionEvaluator(resolvers);
    }

    @Override
    protected MethodSecurityExpressionHandler createExpressionHandler() {
        DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
        handler.setRoleHierarchy(roleHierarchy());
        handler.setPermissionEvaluator(permissionEvaluator());
        return handler;
    }
}
