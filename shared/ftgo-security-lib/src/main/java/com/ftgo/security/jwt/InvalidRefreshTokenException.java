package com.ftgo.security.jwt;

import org.springframework.security.core.AuthenticationException;

/**
 * Thrown when a refresh token is invalid, expired, or not actually
 * a refresh token.
 */
public class InvalidRefreshTokenException extends AuthenticationException {

    public InvalidRefreshTokenException(String message) {
        super(message);
    }

    public InvalidRefreshTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
