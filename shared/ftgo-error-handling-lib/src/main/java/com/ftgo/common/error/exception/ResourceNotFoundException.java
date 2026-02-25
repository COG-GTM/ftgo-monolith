package com.ftgo.common.error.exception;

import com.ftgo.common.error.code.CommonErrorCode;
import com.ftgo.common.error.code.ErrorCode;

/**
 * Exception thrown when a requested resource is not found.
 *
 * <p>Maps to HTTP 404 Not Found.</p>
 *
 * <p>Usage:</p>
 * <pre>
 * throw new ResourceNotFoundException("Order", orderId);
 * throw new ResourceNotFoundException(OrderErrorCode.ORDER_NOT_FOUND, "Order 123 not found");
 * </pre>
 */
public class ResourceNotFoundException extends FtgoException {

    private final String resourceType;
    private final Object resourceId;

    public ResourceNotFoundException(String resourceType, Object resourceId) {
        super(CommonErrorCode.RESOURCE_NOT_FOUND,
                String.format("%s with id '%s' was not found", resourceType, resourceId));
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    public ResourceNotFoundException(ErrorCode errorCode, String message) {
        super(errorCode, message);
        this.resourceType = null;
        this.resourceId = null;
    }

    public ResourceNotFoundException(String message) {
        super(CommonErrorCode.RESOURCE_NOT_FOUND, message);
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
