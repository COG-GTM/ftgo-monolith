package com.ftgo.common.error.exception;

import com.ftgo.common.error.code.CommonErrorCode;

/**
 * Exception thrown when an upstream service call times out.
 *
 * <p>Maps to HTTP 504 Gateway Timeout.</p>
 */
public class ServiceTimeoutException extends FtgoException {

    private final String serviceName;

    public ServiceTimeoutException(String serviceName) {
        super(CommonErrorCode.SERVICE_TIMEOUT,
                String.format("Request to '%s' service timed out", serviceName));
        this.serviceName = serviceName;
    }

    public ServiceTimeoutException(String serviceName, Throwable cause) {
        super(CommonErrorCode.SERVICE_TIMEOUT,
                String.format("Request to '%s' service timed out", serviceName),
                cause);
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }
}
