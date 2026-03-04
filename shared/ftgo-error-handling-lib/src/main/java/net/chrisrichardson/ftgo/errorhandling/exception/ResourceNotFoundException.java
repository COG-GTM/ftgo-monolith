package net.chrisrichardson.ftgo.errorhandling.exception;

/**
 * Generic exception for when a requested resource is not found.
 *
 * <p>Services should extend this for domain-specific "not found" exceptions
 * (e.g., OrderNotFoundException, ConsumerNotFoundException) or use directly.</p>
 */
public class ResourceNotFoundException extends RuntimeException {

    private final String resourceType;
    private final String resourceId;

    /**
     * Creates a new ResourceNotFoundException.
     *
     * @param resourceType the type of resource (e.g., "Order", "Consumer")
     * @param resourceId   the identifier of the resource
     */
    public ResourceNotFoundException(String resourceType, String resourceId) {
        super(resourceType + " with id " + resourceId + " not found");
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getResourceId() {
        return resourceId;
    }
}
