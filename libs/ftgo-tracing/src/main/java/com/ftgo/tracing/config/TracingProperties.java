package com.ftgo.tracing.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ftgo.tracing")
public class TracingProperties {

    private boolean enabled = true;
    private String serviceName;
    private double samplingProbability = 1.0;
    private Propagation propagation = new Propagation();
    private Exporter exporter = new Exporter();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public double getSamplingProbability() {
        return samplingProbability;
    }

    public void setSamplingProbability(double samplingProbability) {
        this.samplingProbability = samplingProbability;
    }

    public Propagation getPropagation() {
        return propagation;
    }

    public void setPropagation(Propagation propagation) {
        this.propagation = propagation;
    }

    public Exporter getExporter() {
        return exporter;
    }

    public void setExporter(Exporter exporter) {
        this.exporter = exporter;
    }

    public static class Propagation {

        private PropagationType type = PropagationType.W3C;
        private boolean baggageEnabled = true;

        public PropagationType getType() {
            return type;
        }

        public void setType(PropagationType type) {
            this.type = type;
        }

        public boolean isBaggageEnabled() {
            return baggageEnabled;
        }

        public void setBaggageEnabled(boolean baggageEnabled) {
            this.baggageEnabled = baggageEnabled;
        }
    }

    public static class Exporter {

        private ExporterType type = ExporterType.ZIPKIN;
        private String zipkinEndpoint = "http://localhost:9411/api/v2/spans";
        private String otlpEndpoint = "http://localhost:4317";

        public ExporterType getType() {
            return type;
        }

        public void setType(ExporterType type) {
            this.type = type;
        }

        public String getZipkinEndpoint() {
            return zipkinEndpoint;
        }

        public void setZipkinEndpoint(String zipkinEndpoint) {
            this.zipkinEndpoint = zipkinEndpoint;
        }

        public String getOtlpEndpoint() {
            return otlpEndpoint;
        }

        public void setOtlpEndpoint(String otlpEndpoint) {
            this.otlpEndpoint = otlpEndpoint;
        }
    }

    public enum PropagationType {
        W3C, B3, B3_MULTI
    }

    public enum ExporterType {
        ZIPKIN, OTLP, LOGGING
    }
}
