package com.ftgo.security.authorization;

import java.io.Serializable;

/**
 * Strategy interface for determining resource ownership.
 * <p>
 * Services implement this interface to define how ownership is
 * determined for their domain resources. Implementations are
 * registered with {@link FtgoPermissionEvaluator} to enable
 * ownership-based authorization in {@code @PreAuthorize} expressions.
 * </p>
 *
 * <h3>Example Implementation</h3>
 * <pre>
 * &#64;Component
 * public class OrderOwnershipStrategy implements ResourceOwnershipStrategy {
 *
 *     private final OrderRepository orderRepository;
 *
 *     &#64;Override
 *     public boolean isOwner(Long userId, Serializable resourceId) {
 *         return orderRepository.findById((Long) resourceId)
 *                 .map(order -> order.getConsumerId().equals(userId))
 *                 .orElse(false);
 *     }
 *
 *     &#64;Override
 *     public String getResourceType() {
 *         return "Order";
 *     }
 * }
 * </pre>
 *
 * @see FtgoPermissionEvaluator
 */
public interface ResourceOwnershipStrategy {

    /**
     * Checks if the user with the given ID is the owner of the specified resource.
     *
     * @param userId     the ID of the authenticated user
     * @param resourceId the ID of the resource being accessed
     * @return {@code true} if the user owns the resource
     */
    boolean isOwner(Long userId, Serializable resourceId);

    /**
     * Returns the resource type this strategy handles (e.g. "Order", "Restaurant").
     * <p>
     * This value must match the {@code targetType} parameter in
     * {@code hasPermission(authentication, targetId, targetType, permission)}
     * expressions (case-insensitive).
     * </p>
     *
     * @return the resource type name
     */
    String getResourceType();
}
