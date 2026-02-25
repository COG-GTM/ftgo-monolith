package com.ftgo.common.tracing.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for FTGO distributed tracing.
 *
 * <p>These properties control the behavior of the FTGO tracing library
 * and complement Spring Boot's built-in tracing properties.</p>
 *
 * <h3>Property Prefix</h3>
 * <pre>ftgo.tracing.*</pre>
 *
 * <h3>Standard Spring Boot Tracing Properties</h3>
 * <p>In addition to FTGO-specific properties, configure these Spring Boot
 * properties for full tracing control:</p>
 * <pre>
 * management.tracing.sampling.probability=1.0
 * management.tracing.propagation.type=b3
 * management.zipkin.tracing.endpoint=http://zipkin:9411/api/v2/spans
 * </pre>
 */
@ConfigurationProperties(prefix = "ftgo.tracing")
public class TracingProperties {

    /**
     * Enable or disable FTGO tracing auto-configuration.
     * When disabled, no tracing beans are created.
     * Default: true
     */
    private boolean enabled = true;

    /**
     * Enable custom business spans (order flow, delivery flow).
     * When disabled, only framework-level tracing is active.
     * Default: true
     */
    private boolean customSpansEnabled = true;

    /**
     * Service name override. If not set, uses spring.application.name.
     */
    private String serviceName;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isCustomSpansEnabled() {
        return customSpansEnabled;
    }

    public void setCustomSpansEnabled(boolean customSpansEnabled) {
        this.customSpansEnabled = customSpansEnabled;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
}
