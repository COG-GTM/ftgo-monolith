package net.chrisrichardson.ftgo.security.rbac;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;

/**
 * Enables method-level security with {@code @PreAuthorize} / {@code @PostAuthorize}
 * support and registers the custom {@link FtgoMethodSecurityExpressionHandler}
 * so that FTGO-specific SpEL expressions are available in all annotated methods.
 *
 * <p>This configuration is imported by
 * {@link net.chrisrichardson.ftgo.security.config.FtgoSecurityAutoConfiguration}
 * and is therefore activated automatically when a service includes
 * {@code ftgo-security-lib} on its classpath.
 *
 * <p>Example usage in service code:
 * <pre>
 * &#64;PreAuthorize("hasAuthority('order:create')")
 * public Order createOrder(CreateOrderRequest request) { ... }
 *
 * &#64;PreAuthorize("isResourceOwner(#consumerId) or isAdmin()")
 * public Consumer getConsumer(String consumerId) { ... }
 * </pre>
 */
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class FtgoMethodSecurityConfiguration extends GlobalMethodSecurityConfiguration {

    @Override
    protected MethodSecurityExpressionHandler createExpressionHandler() {
        return new FtgoMethodSecurityExpressionHandler();
    }
}
