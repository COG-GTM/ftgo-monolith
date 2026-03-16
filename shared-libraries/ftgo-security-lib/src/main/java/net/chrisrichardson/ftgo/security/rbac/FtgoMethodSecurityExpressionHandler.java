package net.chrisrichardson.ftgo.security.rbac;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;

/**
 * Custom {@link org.springframework.security.access.expression.method.MethodSecurityExpressionHandler}
 * that creates {@link FtgoSecurityExpressionRoot} instances, making FTGO-specific
 * SpEL functions available in {@code @PreAuthorize} and {@code @PostAuthorize}
 * annotations across all services that depend on {@code ftgo-security-lib}.
 *
 * <p>This handler is registered automatically by
 * {@link FtgoMethodSecurityConfiguration}.
 *
 * @see FtgoSecurityExpressionRoot
 */
public class FtgoMethodSecurityExpressionHandler extends DefaultMethodSecurityExpressionHandler {

    private final AuthenticationTrustResolver trustResolver = new AuthenticationTrustResolverImpl();

    @Override
    protected MethodSecurityExpressionOperations createSecurityExpressionRoot(
            Authentication authentication, MethodInvocation invocation) {

        FtgoSecurityExpressionRoot root = new FtgoSecurityExpressionRoot(authentication);
        root.setTrustResolver(trustResolver);
        root.setPermissionEvaluator(getPermissionEvaluator());
        root.setRoleHierarchy(getRoleHierarchy());
        return root;
    }
}
