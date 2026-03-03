package net.chrisrichardson.ftgo.authorization.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to restrict method access to the resource owner or administrators.
 *
 * <p>When applied to a method, the permission evaluator verifies that the
 * authenticated user is the owner of the resource being accessed, or that
 * the user has the ADMIN role. The resource type and ID parameter name
 * must be specified.</p>
 *
 * <p>Example usage:</p>
 * <pre>
 * &#64;RequireResourceOwner(resourceType = "order", resourceIdParam = "orderId")
 * public Order getOrder(&#64;PathVariable Long orderId) { ... }
 *
 * &#64;RequireResourceOwner(resourceType = "consumer", resourceIdParam = "consumerId")
 * public Consumer getConsumer(&#64;PathVariable Long consumerId) { ... }
 * </pre>
 *
 * <p>The resource ownership check is delegated to the
 * {@link net.chrisrichardson.ftgo.authorization.evaluator.FtgoPermissionEvaluator}
 * which uses the configured
 * {@link net.chrisrichardson.ftgo.authorization.evaluator.ResourceOwnershipResolver}
 * to determine ownership.</p>
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireResourceOwner {

    /**
     * The type of resource being accessed (e.g., "order", "consumer", "restaurant").
     *
     * @return the resource type
     */
    String resourceType();

    /**
     * The name of the method parameter that contains the resource ID.
     *
     * @return the parameter name containing the resource ID
     */
    String resourceIdParam() default "id";
}
