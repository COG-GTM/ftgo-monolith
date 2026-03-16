package net.chrisrichardson.ftgo.errorhandling.exception;

import net.chrisrichardson.ftgo.errorhandling.ErrorCode;

/**
 * Thrown when a required downstream service is unavailable.
 *
 * <p>Maps to HTTP 503 Service Unavailable.
 */
public class ServiceUnavailableException extends BaseException {

    public ServiceUnavailableException(String serviceName) {
        super(ErrorCode.SERVICE_UNAVAILABLE,
                "Service unavailable: " + serviceName);
    }

    public ServiceUnavailableException(String serviceName, Throwable cause) {
        super(ErrorCode.SERVICE_UNAVAILABLE,
                "Service unavailable: " + serviceName, cause);
    }
}
