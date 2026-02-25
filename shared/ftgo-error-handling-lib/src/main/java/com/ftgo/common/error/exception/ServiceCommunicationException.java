package com.ftgo.common.error.exception;

import com.ftgo.common.error.code.CommonErrorCode;
import com.ftgo.common.error.code.ErrorCode;

/**
 * Exception thrown when inter-service communication fails.
 *
 * <p>Used to wrap errors from upstream service calls (REST, messaging, etc.)
 * and provide consistent error handling for service-to-service failures.</p>
 *
 * <p>Usage:</p>
 * <pre>
 * try {
 *     restTemplate.getForObject(url, Response.class);
 * } catch (RestClientException e) {
 *     throw new ServiceCommunicationException("restaurant-service", e);
 * }
 * </pre>
 */
public class ServiceCommunicationException extends FtgoException {

    private final String serviceName;

    public ServiceCommunicationException(String serviceName, Throwable cause) {
        super(CommonErrorCode.UPSTREAM_SERVICE_ERROR,
                String.format("Communication with '%s' service failed: %s", serviceName, cause.getMessage()),
                cause);
        this.serviceName = serviceName;
    }

    public ServiceCommunicationException(String serviceName, String message) {
        super(CommonErrorCode.UPSTREAM_SERVICE_ERROR,
                String.format("Communication with '%s' service failed: %s", serviceName, message));
        this.serviceName = serviceName;
    }

    public ServiceCommunicationException(ErrorCode errorCode, String serviceName, String message) {
        super(errorCode, String.format("Communication with '%s' service failed: %s", serviceName, message));
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }
}
