package net.chrisrichardson.ftgo.errorhandling.exception;

import net.chrisrichardson.ftgo.errorhandling.ErrorCode;

/**
 * Thrown when an optimistic lock conflict is detected during a concurrent update.
 *
 * <p>Maps to HTTP 409 Conflict.
 */
public class OptimisticLockException extends BaseException {

    public OptimisticLockException(String resourceType, Object resourceId) {
        super(ErrorCode.OPTIMISTIC_LOCK_CONFLICT,
                resourceType + " with id " + resourceId + " was modified by another request");
    }

    public OptimisticLockException(String message) {
        super(ErrorCode.OPTIMISTIC_LOCK_CONFLICT, message);
    }
}
