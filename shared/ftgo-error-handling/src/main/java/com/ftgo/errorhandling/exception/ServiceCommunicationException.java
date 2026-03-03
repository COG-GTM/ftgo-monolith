package com.ftgo.errorhandling.exception;

/**
 * Exception thrown when inter-service communication fails.
 *
 * <p>Maps to HTTP 502 Bad Gateway or 503 Service Unavailable depending
 * on the failure type. Use this for timeout, connection refused, circuit
 * breaker open, or downstream service error scenarios.</p>
 *
 * <p>Example usage:</p>
 * <pre>
 * try {
 *     restTemplate.getForObject(url, Response.class);
 * } catch (ResourceAccessException ex) {
 *     throw new ServiceCommunicationException(
 *         ErrorCodes.SERVICE_TIMEOUT,
 *         "consumer-service",
 *         "Request timed out while validating consumer",
 *         ex
 *     );
 * }
 * </pre>
 */
public class ServiceCommunicationException extends RuntimeException {

    private final String errorCode;
    private final String targetService;

    public ServiceCommunicationException(String errorCode, String targetService, String message) {
        super(message);
        this.errorCode = errorCode;
        this.targetService = targetService;
    }

    public ServiceCommunicationException(String errorCode, String targetService, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.targetService = targetService;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getTargetService() {
        return targetService;
    }
}
