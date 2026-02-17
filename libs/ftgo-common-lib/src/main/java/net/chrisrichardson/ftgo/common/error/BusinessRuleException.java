package net.chrisrichardson.ftgo.common.error;

/**
 * Thrown when a business rule is violated.
 * Results in HTTP 422 response.
 */
public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String message) {
        super(message);
    }
}
