package net.chrisrichardson.ftgo.errorhandling.exception;

/**
 * Exception thrown when inter-service communication fails.
 *
 * <p>This covers scenarios such as:</p>
 * <ul>
 *     <li>Downstream service returning an error response</li>
 *     <li>Connection timeouts to downstream services</li>
 *     <li>Service unavailability</li>
 * </ul>
 */
public class ServiceCommunicationException extends RuntimeException {

    private final String serviceName;
    private final boolean timeout;

    /**
     * Creates a new ServiceCommunicationException.
     *
     * @param serviceName the name of the downstream service that failed
     * @param message     human-readable error description
     */
    public ServiceCommunicationException(String serviceName, String message) {
        super(message);
        this.serviceName = serviceName;
        this.timeout = false;
    }

    /**
     * Creates a new ServiceCommunicationException with a cause.
     *
     * @param serviceName the name of the downstream service that failed
     * @param message     human-readable error description
     * @param cause       the underlying exception
     */
    public ServiceCommunicationException(String serviceName, String message, Throwable cause) {
        super(message, cause);
        this.serviceName = serviceName;
        this.timeout = false;
    }

    /**
     * Creates a new ServiceCommunicationException indicating a timeout.
     *
     * @param serviceName the name of the downstream service that timed out
     * @param message     human-readable error description
     * @param cause       the underlying exception
     * @param timeout     whether this was caused by a timeout
     */
    public ServiceCommunicationException(String serviceName, String message, Throwable cause, boolean timeout) {
        super(message, cause);
        this.serviceName = serviceName;
        this.timeout = timeout;
    }

    public String getServiceName() {
        return serviceName;
    }

    public boolean isTimeout() {
        return timeout;
    }
}
