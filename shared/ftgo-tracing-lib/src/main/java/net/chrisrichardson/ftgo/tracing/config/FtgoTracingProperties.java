package net.chrisrichardson.ftgo.tracing.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for FTGO distributed tracing.
 *
 * <p>Properties are bound from {@code ftgo.tracing.*} in application configuration.
 *
 * <p>Sampling rates:
 * <ul>
 *   <li>Development: 1.0 (100% — trace every request)</li>
 *   <li>Production: 0.1 (10% — sample 1 in 10 requests)</li>
 * </ul>
 *
 * <p>Example configuration:
 * <pre>
 * ftgo:
 *   tracing:
 *     enabled: true
 *     sampling-probability: 1.0
 *     zipkin-endpoint: http://zipkin:9411/api/v2/spans
 *     propagation-type: B3
 *     service-name: ftgo-order-service
 * </pre>
 */
@ConfigurationProperties(prefix = "ftgo.tracing")
public class FtgoTracingProperties {

    /**
     * Whether distributed tracing is enabled. Defaults to {@code true}.
     */
    private boolean enabled = true;

    /**
     * Sampling probability (0.0 to 1.0). Determines what fraction of requests
     * are traced. Defaults to {@code 1.0} (100%).
     */
    private float samplingProbability = 1.0f;

    /**
     * Zipkin collector endpoint for reporting spans.
     * Defaults to {@code http://localhost:9411/api/v2/spans}.
     */
    private String zipkinEndpoint = "http://localhost:9411/api/v2/spans";

    /**
     * Trace context propagation type. Supported values: B3, W3C.
     * Defaults to {@code B3} for Zipkin compatibility.
     */
    private String propagationType = "B3";

    /**
     * Service name reported in traces. If not set, defaults to
     * {@code spring.application.name}.
     */
    private String serviceName;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public float getSamplingProbability() {
        return samplingProbability;
    }

    public void setSamplingProbability(float samplingProbability) {
        this.samplingProbability = samplingProbability;
    }

    public String getZipkinEndpoint() {
        return zipkinEndpoint;
    }

    public void setZipkinEndpoint(String zipkinEndpoint) {
        this.zipkinEndpoint = zipkinEndpoint;
    }

    public String getPropagationType() {
        return propagationType;
    }

    public void setPropagationType(String propagationType) {
        this.propagationType = propagationType;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
}
