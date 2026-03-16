package net.chrisrichardson.ftgo.errorhandling.exception;

import net.chrisrichardson.ftgo.errorhandling.ErrorCode;

/**
 * Thrown when authentication fails or credentials are missing.
 *
 * <p>Maps to HTTP 401 Unauthorized.
 */
public class AuthenticationException extends BaseException {

    public AuthenticationException() {
        super(ErrorCode.UNAUTHORIZED);
    }

    public AuthenticationException(String message) {
        super(ErrorCode.UNAUTHORIZED, message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(ErrorCode.UNAUTHORIZED, message, cause);
    }
}
