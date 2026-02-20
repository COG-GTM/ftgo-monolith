package com.ftgo.observability.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ftgo.observability")
public class ObservabilityProperties {

    private String applicationName;
    private String environment;
    private Metrics metrics = new Metrics();

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public Metrics getMetrics() {
        return metrics;
    }

    public void setMetrics(Metrics metrics) {
        this.metrics = metrics;
    }

    public static class Metrics {

        private boolean enabled = true;
        private boolean businessMetricsEnabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isBusinessMetricsEnabled() {
            return businessMetricsEnabled;
        }

        public void setBusinessMetricsEnabled(boolean businessMetricsEnabled) {
            this.businessMetricsEnabled = businessMetricsEnabled;
        }
    }
}
