package com.ftgo.errorhandling.exception;

/**
 * Exception thrown when a requested resource cannot be found.
 *
 * <p>Maps to HTTP 404 Not Found. Use this for any entity lookup
 * that fails (e.g., order, consumer, restaurant, courier not found).</p>
 *
 * <p>Example usage:</p>
 * <pre>
 * Order order = orderRepository.findById(orderId)
 *     .orElseThrow(() -&gt; new ResourceNotFoundException("Order", orderId));
 * </pre>
 */
public class ResourceNotFoundException extends RuntimeException {

    private final String resourceType;
    private final Object resourceId;

    public ResourceNotFoundException(String resourceType, Object resourceId) {
        super(String.format("%s not found with id: %s", resourceType, resourceId));
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    public ResourceNotFoundException(String message) {
        super(message);
        this.resourceType = null;
        this.resourceId = null;
    }

    public String getResourceType() {
        return resourceType;
    }

    public Object getResourceId() {
        return resourceId;
    }
}
