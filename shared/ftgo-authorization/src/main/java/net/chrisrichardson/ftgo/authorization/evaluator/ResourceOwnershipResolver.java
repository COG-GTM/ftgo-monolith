package net.chrisrichardson.ftgo.authorization.evaluator;

import java.io.Serializable;

/**
 * Strategy interface for resolving resource ownership.
 *
 * <p>Implementations of this interface provide service-specific logic to
 * determine which user owns a given resource. Each microservice registers
 * its own resolver to handle ownership checks for its domain objects.</p>
 *
 * <p>Example implementation for the Order Service:</p>
 * <pre>
 * &#64;Component
 * public class OrderOwnershipResolver implements ResourceOwnershipResolver {
 *     &#64;Override
 *     public boolean supports(String resourceType) {
 *         return "order".equals(resourceType);
 *     }
 *
 *     &#64;Override
 *     public boolean isOwner(String userId, Serializable resourceId) {
 *         Order order = orderRepository.findById((Long) resourceId);
 *         return order != null &amp;&amp; order.getConsumerId().equals(Long.parseLong(userId));
 *     }
 * }
 * </pre>
 *
 * @see FtgoPermissionEvaluator
 */
public interface ResourceOwnershipResolver {

    /**
     * Returns whether this resolver handles the given resource type.
     *
     * @param resourceType the type of resource (e.g., "order", "consumer", "restaurant")
     * @return true if this resolver can evaluate ownership for the resource type
     */
    boolean supports(String resourceType);

    /**
     * Determines whether the specified user owns the given resource.
     *
     * @param userId the authenticated user's ID
     * @param resourceId the resource identifier
     * @return true if the user is the owner of the resource
     */
    boolean isOwner(String userId, Serializable resourceId);
}
