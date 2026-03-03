package com.ftgo.tracing.config;

import com.ftgo.tracing.TracingConstants;

/**
 * Configuration properties for FTGO distributed tracing.
 *
 * <p>Properties are bound from the {@code ftgo.tracing} prefix in
 * application.yml or application.properties. Services can customize
 * tracing behavior by setting these properties.</p>
 *
 * <h3>Example Configuration (application.yml):</h3>
 * <pre>
 * ftgo:
 *   tracing:
 *     enabled: true
 *     sampling-probability: 1.0
 *     zipkin-endpoint: http://zipkin:9411/api/v2/spans
 *     propagation-type: B3
 *     service-name: order-service
 * </pre>
 */
public class FtgoTracingProperties {

    /** Whether distributed tracing is enabled. Defaults to true. */
    private boolean enabled = true;

    /**
     * Sampling probability (0.0 to 1.0).
     * 1.0 = sample all traces (recommended for dev).
     * 0.1 = sample 10% of traces (recommended for production).
     */
    private float samplingProbability = TracingConstants.DEFAULT_SAMPLING_PROBABILITY_DEV;

    /** Zipkin collector endpoint URL. */
    private String zipkinEndpoint = TracingConstants.DEFAULT_ZIPKIN_ENDPOINT;

    /**
     * Trace context propagation type.
     * Supported values: B3 (default), W3C.
     */
    private String propagationType = "B3";

    /**
     * Service name override. If not set, falls back to spring.application.name.
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
