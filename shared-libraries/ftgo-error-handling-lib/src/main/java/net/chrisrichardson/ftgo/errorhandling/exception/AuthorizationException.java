package net.chrisrichardson.ftgo.errorhandling.exception;

import net.chrisrichardson.ftgo.errorhandling.ErrorCode;

/**
 * Thrown when the authenticated user lacks permission for the requested action.
 *
 * <p>Maps to HTTP 403 Forbidden.
 */
public class AuthorizationException extends BaseException {

    public AuthorizationException() {
        super(ErrorCode.FORBIDDEN);
    }

    public AuthorizationException(String message) {
        super(ErrorCode.FORBIDDEN, message);
    }
}
