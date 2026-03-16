package net.chrisrichardson.ftgo.errorhandling.exception;

import net.chrisrichardson.ftgo.errorhandling.ErrorCode;

/**
 * Thrown when a requested resource cannot be found.
 *
 * <p>Maps to HTTP 404 Not Found.
 */
public class ResourceNotFoundException extends BaseException {

    public ResourceNotFoundException(String resourceType, Object resourceId) {
        super(ErrorCode.RESOURCE_NOT_FOUND,
                resourceType + " not found with id: " + resourceId);
    }

    public ResourceNotFoundException(String message) {
        super(ErrorCode.RESOURCE_NOT_FOUND, message);
    }
}
