package net.chrisrichardson.ftgo.common.error;

/**
 * Thrown when attempting to create a resource that already exists.
 * Results in HTTP 409 response.
 */
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
