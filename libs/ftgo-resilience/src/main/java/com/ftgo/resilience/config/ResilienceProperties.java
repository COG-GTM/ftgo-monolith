package com.ftgo.resilience.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ftgo.resilience")
public class ResilienceProperties {

    private boolean enabled = true;
    private CircuitBreaker circuitBreaker = new CircuitBreaker();
    private Retry retry = new Retry();
    private Bulkhead bulkhead = new Bulkhead();
    private HealthCheck healthCheck = new HealthCheck();
    private Discovery discovery = new Discovery();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public CircuitBreaker getCircuitBreaker() {
        return circuitBreaker;
    }

    public void setCircuitBreaker(CircuitBreaker circuitBreaker) {
        this.circuitBreaker = circuitBreaker;
    }

    public Retry getRetry() {
        return retry;
    }

    public void setRetry(Retry retry) {
        this.retry = retry;
    }

    public Bulkhead getBulkhead() {
        return bulkhead;
    }

    public void setBulkhead(Bulkhead bulkhead) {
        this.bulkhead = bulkhead;
    }

    public HealthCheck getHealthCheck() {
        return healthCheck;
    }

    public void setHealthCheck(HealthCheck healthCheck) {
        this.healthCheck = healthCheck;
    }

    public Discovery getDiscovery() {
        return discovery;
    }

    public void setDiscovery(Discovery discovery) {
        this.discovery = discovery;
    }

    public static class CircuitBreaker {

        private boolean enabled = true;
        private int failureRateThreshold = 50;
        private int slowCallRateThreshold = 80;
        private int slowCallDurationThresholdMs = 2000;
        private int slidingWindowSize = 10;
        private int minimumNumberOfCalls = 5;
        private int waitDurationInOpenStateMs = 30000;
        private int permittedNumberOfCallsInHalfOpenState = 3;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getFailureRateThreshold() {
            return failureRateThreshold;
        }

        public void setFailureRateThreshold(int failureRateThreshold) {
            this.failureRateThreshold = failureRateThreshold;
        }

        public int getSlowCallRateThreshold() {
            return slowCallRateThreshold;
        }

        public void setSlowCallRateThreshold(int slowCallRateThreshold) {
            this.slowCallRateThreshold = slowCallRateThreshold;
        }

        public int getSlowCallDurationThresholdMs() {
            return slowCallDurationThresholdMs;
        }

        public void setSlowCallDurationThresholdMs(int slowCallDurationThresholdMs) {
            this.slowCallDurationThresholdMs = slowCallDurationThresholdMs;
        }

        public int getSlidingWindowSize() {
            return slidingWindowSize;
        }

        public void setSlidingWindowSize(int slidingWindowSize) {
            this.slidingWindowSize = slidingWindowSize;
        }

        public int getMinimumNumberOfCalls() {
            return minimumNumberOfCalls;
        }

        public void setMinimumNumberOfCalls(int minimumNumberOfCalls) {
            this.minimumNumberOfCalls = minimumNumberOfCalls;
        }

        public int getWaitDurationInOpenStateMs() {
            return waitDurationInOpenStateMs;
        }

        public void setWaitDurationInOpenStateMs(int waitDurationInOpenStateMs) {
            this.waitDurationInOpenStateMs = waitDurationInOpenStateMs;
        }

        public int getPermittedNumberOfCallsInHalfOpenState() {
            return permittedNumberOfCallsInHalfOpenState;
        }

        public void setPermittedNumberOfCallsInHalfOpenState(int permittedNumberOfCallsInHalfOpenState) {
            this.permittedNumberOfCallsInHalfOpenState = permittedNumberOfCallsInHalfOpenState;
        }
    }

    public static class Retry {

        private boolean enabled = true;
        private int maxAttempts = 3;
        private int waitDurationMs = 500;
        private double multiplier = 2.0;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getMaxAttempts() {
            return maxAttempts;
        }

        public void setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }

        public int getWaitDurationMs() {
            return waitDurationMs;
        }

        public void setWaitDurationMs(int waitDurationMs) {
            this.waitDurationMs = waitDurationMs;
        }

        public double getMultiplier() {
            return multiplier;
        }

        public void setMultiplier(double multiplier) {
            this.multiplier = multiplier;
        }
    }

    public static class Bulkhead {

        private boolean enabled = true;
        private int maxConcurrentCalls = 25;
        private int maxWaitDurationMs = 500;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getMaxConcurrentCalls() {
            return maxConcurrentCalls;
        }

        public void setMaxConcurrentCalls(int maxConcurrentCalls) {
            this.maxConcurrentCalls = maxConcurrentCalls;
        }

        public int getMaxWaitDurationMs() {
            return maxWaitDurationMs;
        }

        public void setMaxWaitDurationMs(int maxWaitDurationMs) {
            this.maxWaitDurationMs = maxWaitDurationMs;
        }
    }

    public static class HealthCheck {

        private boolean enabled = true;
        private boolean databaseEnabled = true;
        private boolean messagingEnabled = true;
        private boolean externalServicesEnabled = true;
        private int timeoutMs = 5000;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isDatabaseEnabled() {
            return databaseEnabled;
        }

        public void setDatabaseEnabled(boolean databaseEnabled) {
            this.databaseEnabled = databaseEnabled;
        }

        public boolean isMessagingEnabled() {
            return messagingEnabled;
        }

        public void setMessagingEnabled(boolean messagingEnabled) {
            this.messagingEnabled = messagingEnabled;
        }

        public boolean isExternalServicesEnabled() {
            return externalServicesEnabled;
        }

        public void setExternalServicesEnabled(boolean externalServicesEnabled) {
            this.externalServicesEnabled = externalServicesEnabled;
        }

        public int getTimeoutMs() {
            return timeoutMs;
        }

        public void setTimeoutMs(int timeoutMs) {
            this.timeoutMs = timeoutMs;
        }
    }

    public static class Discovery {

        private boolean enabled = true;
        private String type = "kubernetes";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }
}
