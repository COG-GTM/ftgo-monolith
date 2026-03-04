package com.ftgo.security.authorization;

/**
 * Interface for domain objects that have an owner.
 *
 * <p>Implementing this interface allows the {@link FtgoPermissionEvaluator}
 * to perform resource ownership validation. The evaluator compares the
 * authenticated user's ID with the value returned by {@link #getOwnerId()}.
 *
 * <p>Example implementation:
 * <pre>
 * public class Order implements ResourceOwner {
 *     private String customerId;
 *
 *     &#064;Override
 *     public String getOwnerId() {
 *         return customerId;
 *     }
 * }
 * </pre>
 *
 * @see FtgoPermissionEvaluator
 */
public interface ResourceOwner {

    /**
     * Returns the user ID of the resource owner.
     *
     * <p>This value is compared against the authenticated user's ID
     * (from JWT {@code sub} claim) to determine ownership.
     *
     * @return the owner's user ID
     */
    String getOwnerId();
}
