package com.ftgo.observability.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@AutoConfiguration
@ConditionalOnClass(name = "org.springframework.boot.actuate.endpoint.annotation.Endpoint")
public class ActuatorEndpointConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "actuatorEndpointProperties")
    public ActuatorEndpointProperties actuatorEndpointProperties() {
        return new ActuatorEndpointProperties();
    }

    public static class ActuatorEndpointProperties {

        private String basePath = "/actuator";
        private boolean prometheusEnabled = true;
        private boolean healthEnabled = true;
        private boolean infoEnabled = true;
        private boolean metricsEnabled = true;

        public String getBasePath() {
            return basePath;
        }

        public void setBasePath(String basePath) {
            this.basePath = basePath;
        }

        public boolean isPrometheusEnabled() {
            return prometheusEnabled;
        }

        public void setPrometheusEnabled(boolean prometheusEnabled) {
            this.prometheusEnabled = prometheusEnabled;
        }

        public boolean isHealthEnabled() {
            return healthEnabled;
        }

        public void setHealthEnabled(boolean healthEnabled) {
            this.healthEnabled = healthEnabled;
        }

        public boolean isInfoEnabled() {
            return infoEnabled;
        }

        public void setInfoEnabled(boolean infoEnabled) {
            this.infoEnabled = infoEnabled;
        }

        public boolean isMetricsEnabled() {
            return metricsEnabled;
        }

        public void setMetricsEnabled(boolean metricsEnabled) {
            this.metricsEnabled = metricsEnabled;
        }
    }
}
