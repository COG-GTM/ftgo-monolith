package net.chrisrichardson.ftgo.authorization.annotation;

import net.chrisrichardson.ftgo.authorization.model.FtgoPermission;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to restrict method access to users with specific permissions.
 *
 * <p>This annotation is evaluated by Spring Security's method-level security
 * infrastructure. It provides fine-grained access control beyond role checks.</p>
 *
 * <p>Example usage:</p>
 * <pre>
 * &#64;RequirePermission(FtgoPermission.ORDER_CREATE)
 * public Order createOrder(CreateOrderRequest request) { ... }
 *
 * &#64;RequirePermission(value = {FtgoPermission.RESTAURANT_READ, FtgoPermission.RESTAURANT_UPDATE})
 * public Restaurant updateRestaurant(Long id, UpdateRequest request) { ... }
 * </pre>
 *
 * <p>When multiple permissions are specified, access is granted if the user has
 * <strong>any</strong> of the listed permissions (OR logic).</p>
 *
 * @see FtgoPermission
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {

    /**
     * The permissions required to access the annotated method.
     * Access is granted if the user has any of the specified permissions.
     *
     * @return the required permissions
     */
    FtgoPermission[] value();
}
