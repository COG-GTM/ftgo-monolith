package net.chrisrichardson.ftgo.authorization.annotation;

import net.chrisrichardson.ftgo.authorization.model.FtgoRole;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to restrict method access to users with specific roles.
 *
 * <p>This annotation is evaluated by Spring Security's method-level security
 * infrastructure. It can be applied to controller methods or service methods
 * to enforce role-based access control.</p>
 *
 * <p>Example usage:</p>
 * <pre>
 * &#64;RequireRole(FtgoRole.ADMIN)
 * public void deleteUser(Long userId) { ... }
 *
 * &#64;RequireRole(value = {FtgoRole.CUSTOMER, FtgoRole.ADMIN})
 * public Order createOrder(CreateOrderRequest request) { ... }
 * </pre>
 *
 * <p>When multiple roles are specified, access is granted if the user has
 * <strong>any</strong> of the listed roles (OR logic).</p>
 *
 * @see FtgoRole
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireRole {

    /**
     * The roles that are allowed to access the annotated method.
     * Access is granted if the user has any of the specified roles.
     *
     * @return the required roles
     */
    FtgoRole[] value();
}
