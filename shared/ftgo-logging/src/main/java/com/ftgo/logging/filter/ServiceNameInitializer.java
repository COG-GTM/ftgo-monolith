package com.ftgo.logging.filter;

import com.ftgo.logging.LoggingConstants;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

/**
 * Initializes the service name in the MDC context when the application starts.
 * The service name is derived from the {@code spring.application.name} property.
 *
 * <p>This ensures that all log entries include the service name field, enabling
 * cross-service log correlation and filtering in the EFK stack.</p>
 */
public class ServiceNameInitializer {

    private final String serviceName;

    public ServiceNameInitializer(@Value("${spring.application.name:unknown-service}") String serviceName) {
        this.serviceName = serviceName;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        MDC.put(LoggingConstants.MDC_SERVICE_NAME, serviceName);
    }

    /**
     * Returns the configured service name.
     *
     * @return the service name
     */
    public String getServiceName() {
        return serviceName;
    }
}
