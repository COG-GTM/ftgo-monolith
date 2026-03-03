package net.chrisrichardson.ftgo.authorization.model;

import java.io.Serializable;

/**
 * Represents a resource ownership claim used for authorization decisions.
 *
 * <p>When a user attempts to access a resource, the permission evaluator checks
 * whether the user owns or is associated with the resource. This object
 * encapsulates the ownership information needed for that check.</p>
 *
 * <p>Example: A CUSTOMER accessing order #123 produces a ResourceOwnership of:
 * {@code ResourceOwnership("order", "123", "consumer-id-456")}</p>
 */
public class ResourceOwnership implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String resourceType;
    private final String resourceId;
    private final String ownerId;

    public ResourceOwnership(String resourceType, String resourceId, String ownerId) {
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.ownerId = ownerId;
    }

    /**
     * Returns the type of resource (e.g., "order", "restaurant", "consumer").
     *
     * @return the resource type
     */
    public String getResourceType() {
        return resourceType;
    }

    /**
     * Returns the unique identifier of the resource.
     *
     * @return the resource ID
     */
    public String getResourceId() {
        return resourceId;
    }

    /**
     * Returns the user ID of the resource owner.
     *
     * @return the owner's user ID
     */
    public String getOwnerId() {
        return ownerId;
    }

    @Override
    public String toString() {
        return "ResourceOwnership{" +
                "resourceType='" + resourceType + '\'' +
                ", resourceId='" + resourceId + '\'' +
                ", ownerId='" + ownerId + '\'' +
                '}';
    }
}
