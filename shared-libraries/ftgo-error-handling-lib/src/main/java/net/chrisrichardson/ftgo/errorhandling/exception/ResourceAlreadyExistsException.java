package net.chrisrichardson.ftgo.errorhandling.exception;

import net.chrisrichardson.ftgo.errorhandling.ErrorCode;

/**
 * Thrown when attempting to create a resource that already exists.
 *
 * <p>Maps to HTTP 409 Conflict.
 */
public class ResourceAlreadyExistsException extends BaseException {

    public ResourceAlreadyExistsException(String resourceType, Object resourceId) {
        super(ErrorCode.RESOURCE_ALREADY_EXISTS,
                resourceType + " already exists with id: " + resourceId);
    }

    public ResourceAlreadyExistsException(String message) {
        super(ErrorCode.RESOURCE_ALREADY_EXISTS, message);
    }
}
