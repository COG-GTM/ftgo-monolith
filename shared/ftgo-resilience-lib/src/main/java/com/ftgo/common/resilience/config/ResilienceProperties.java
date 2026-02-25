package com.ftgo.common.resilience.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for FTGO resilience library.
 *
 * <p>These properties control the behavior of circuit breakers, retries,
 * bulkheads, rate limiters, health checks, and service discovery.</p>
 *
 * <h3>Property Prefix</h3>
 * <pre>ftgo.resilience.*</pre>
 */
@ConfigurationProperties(prefix = "ftgo.resilience")
public class ResilienceProperties {

    /**
     * Enable or disable FTGO resilience auto-configuration.
     * When disabled, no resilience beans are created.
     * Default: true
     */
    private boolean enabled = true;

    /**
     * Circuit breaker configuration.
     */
    private CircuitBreakerProperties circuitBreaker = new CircuitBreakerProperties();

    /**
     * Retry configuration.
     */
    private RetryProperties retry = new RetryProperties();

    /**
     * Bulkhead configuration.
     */
    private BulkheadProperties bulkhead = new BulkheadProperties();

    /**
     * Rate limiter configuration.
     */
    private RateLimiterProperties rateLimiter = new RateLimiterProperties();

    /**
     * Health check configuration.
     */
    private HealthProperties health = new HealthProperties();

    /**
     * Service discovery configuration.
     */
    private DiscoveryProperties discovery = new DiscoveryProperties();

    /**
     * Graceful shutdown configuration.
     */
    private ShutdownProperties shutdown = new ShutdownProperties();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public CircuitBreakerProperties getCircuitBreaker() {
        return circuitBreaker;
    }

    public void setCircuitBreaker(CircuitBreakerProperties circuitBreaker) {
        this.circuitBreaker = circuitBreaker;
    }

    public RetryProperties getRetry() {
        return retry;
    }

    public void setRetry(RetryProperties retry) {
        this.retry = retry;
    }

    public BulkheadProperties getBulkhead() {
        return bulkhead;
    }

    public void setBulkhead(BulkheadProperties bulkhead) {
        this.bulkhead = bulkhead;
    }

    public RateLimiterProperties getRateLimiter() {
        return rateLimiter;
    }

    public void setRateLimiter(RateLimiterProperties rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    public HealthProperties getHealth() {
        return health;
    }

    public void setHealth(HealthProperties health) {
        this.health = health;
    }

    public DiscoveryProperties getDiscovery() {
        return discovery;
    }

    public void setDiscovery(DiscoveryProperties discovery) {
        this.discovery = discovery;
    }

    public ShutdownProperties getShutdown() {
        return shutdown;
    }

    public void setShutdown(ShutdownProperties shutdown) {
        this.shutdown = shutdown;
    }

    /**
     * Circuit breaker properties.
     */
    public static class CircuitBreakerProperties {

        /** Number of failures before the circuit opens. Default: 5 */
        private int failureRateThreshold = 50;

        /** Minimum number of calls before evaluating failure rate. Default: 5 */
        private int minimumNumberOfCalls = 5;

        /** Number of permitted calls in half-open state. Default: 3 */
        private int permittedNumberOfCallsInHalfOpenState = 3;

        /** Wait duration in seconds before transitioning from open to half-open. Default: 30 */
        private int waitDurationInOpenStateSeconds = 30;

        /** Sliding window size for failure rate calculation. Default: 10 */
        private int slidingWindowSize = 10;

        /** Sliding window type: COUNT_BASED or TIME_BASED. Default: COUNT_BASED */
        private String slidingWindowType = "COUNT_BASED";

        /** Slow call duration threshold in seconds. Default: 2 */
        private int slowCallDurationThresholdSeconds = 2;

        /** Slow call rate threshold percentage. Default: 80 */
        private int slowCallRateThreshold = 80;

        public int getFailureRateThreshold() {
            return failureRateThreshold;
        }

        public void setFailureRateThreshold(int failureRateThreshold) {
            this.failureRateThreshold = failureRateThreshold;
        }

        public int getMinimumNumberOfCalls() {
            return minimumNumberOfCalls;
        }

        public void setMinimumNumberOfCalls(int minimumNumberOfCalls) {
            this.minimumNumberOfCalls = minimumNumberOfCalls;
        }

        public int getPermittedNumberOfCallsInHalfOpenState() {
            return permittedNumberOfCallsInHalfOpenState;
        }

        public void setPermittedNumberOfCallsInHalfOpenState(int permittedNumberOfCallsInHalfOpenState) {
            this.permittedNumberOfCallsInHalfOpenState = permittedNumberOfCallsInHalfOpenState;
        }

        public int getWaitDurationInOpenStateSeconds() {
            return waitDurationInOpenStateSeconds;
        }

        public void setWaitDurationInOpenStateSeconds(int waitDurationInOpenStateSeconds) {
            this.waitDurationInOpenStateSeconds = waitDurationInOpenStateSeconds;
        }

        public int getSlidingWindowSize() {
            return slidingWindowSize;
        }

        public void setSlidingWindowSize(int slidingWindowSize) {
            this.slidingWindowSize = slidingWindowSize;
        }

        public String getSlidingWindowType() {
            return slidingWindowType;
        }

        public void setSlidingWindowType(String slidingWindowType) {
            this.slidingWindowType = slidingWindowType;
        }

        public int getSlowCallDurationThresholdSeconds() {
            return slowCallDurationThresholdSeconds;
        }

        public void setSlowCallDurationThresholdSeconds(int slowCallDurationThresholdSeconds) {
            this.slowCallDurationThresholdSeconds = slowCallDurationThresholdSeconds;
        }

        public int getSlowCallRateThreshold() {
            return slowCallRateThreshold;
        }

        public void setSlowCallRateThreshold(int slowCallRateThreshold) {
            this.slowCallRateThreshold = slowCallRateThreshold;
        }
    }

    /**
     * Retry properties.
     */
    public static class RetryProperties {

        /** Maximum number of retry attempts. Default: 3 */
        private int maxAttempts = 3;

        /** Initial wait duration in milliseconds for exponential backoff. Default: 1000 */
        private long initialIntervalMillis = 1000;

        /** Multiplier for exponential backoff. Default: 2.0 */
        private double multiplier = 2.0;

        /** Maximum wait duration in milliseconds. Default: 8000 */
        private long maxIntervalMillis = 8000;

        public int getMaxAttempts() {
            return maxAttempts;
        }

        public void setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }

        public long getInitialIntervalMillis() {
            return initialIntervalMillis;
        }

        public void setInitialIntervalMillis(long initialIntervalMillis) {
            this.initialIntervalMillis = initialIntervalMillis;
        }

        public double getMultiplier() {
            return multiplier;
        }

        public void setMultiplier(double multiplier) {
            this.multiplier = multiplier;
        }

        public long getMaxIntervalMillis() {
            return maxIntervalMillis;
        }

        public void setMaxIntervalMillis(long maxIntervalMillis) {
            this.maxIntervalMillis = maxIntervalMillis;
        }
    }

    /**
     * Bulkhead properties.
     */
    public static class BulkheadProperties {

        /** Maximum number of concurrent calls. Default: 25 */
        private int maxConcurrentCalls = 25;

        /** Maximum wait duration in milliseconds for a permit. Default: 500 */
        private long maxWaitDurationMillis = 500;

        public int getMaxConcurrentCalls() {
            return maxConcurrentCalls;
        }

        public void setMaxConcurrentCalls(int maxConcurrentCalls) {
            this.maxConcurrentCalls = maxConcurrentCalls;
        }

        public long getMaxWaitDurationMillis() {
            return maxWaitDurationMillis;
        }

        public void setMaxWaitDurationMillis(long maxWaitDurationMillis) {
            this.maxWaitDurationMillis = maxWaitDurationMillis;
        }
    }

    /**
     * Rate limiter properties.
     */
    public static class RateLimiterProperties {

        /** Maximum number of calls in the refresh period. Default: 50 */
        private int limitForPeriod = 50;

        /** Refresh period in milliseconds. Default: 1000 (1 second) */
        private long limitRefreshPeriodMillis = 1000;

        /** Maximum wait duration for a permit in milliseconds. Default: 500 */
        private long timeoutDurationMillis = 500;

        public int getLimitForPeriod() {
            return limitForPeriod;
        }

        public void setLimitForPeriod(int limitForPeriod) {
            this.limitForPeriod = limitForPeriod;
        }

        public long getLimitRefreshPeriodMillis() {
            return limitRefreshPeriodMillis;
        }

        public void setLimitRefreshPeriodMillis(long limitRefreshPeriodMillis) {
            this.limitRefreshPeriodMillis = limitRefreshPeriodMillis;
        }

        public long getTimeoutDurationMillis() {
            return timeoutDurationMillis;
        }

        public void setTimeoutDurationMillis(long timeoutDurationMillis) {
            this.timeoutDurationMillis = timeoutDurationMillis;
        }
    }

    /**
     * Health check properties.
     */
    public static class HealthProperties {

        /** Enable custom health indicators. Default: true */
        private boolean enabled = true;

        /** Enable downstream service health checks. Default: true */
        private boolean downstreamChecksEnabled = true;

        /** Timeout in seconds for downstream health checks. Default: 5 */
        private int downstreamTimeoutSeconds = 5;

        /** Enable disk space health check. Default: true */
        private boolean diskSpaceEnabled = true;

        /** Disk space threshold in megabytes. Default: 100 */
        private long diskSpaceThresholdMb = 100;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isDownstreamChecksEnabled() {
            return downstreamChecksEnabled;
        }

        public void setDownstreamChecksEnabled(boolean downstreamChecksEnabled) {
            this.downstreamChecksEnabled = downstreamChecksEnabled;
        }

        public int getDownstreamTimeoutSeconds() {
            return downstreamTimeoutSeconds;
        }

        public void setDownstreamTimeoutSeconds(int downstreamTimeoutSeconds) {
            this.downstreamTimeoutSeconds = downstreamTimeoutSeconds;
        }

        public boolean isDiskSpaceEnabled() {
            return diskSpaceEnabled;
        }

        public void setDiskSpaceEnabled(boolean diskSpaceEnabled) {
            this.diskSpaceEnabled = diskSpaceEnabled;
        }

        public long getDiskSpaceThresholdMb() {
            return diskSpaceThresholdMb;
        }

        public void setDiskSpaceThresholdMb(long diskSpaceThresholdMb) {
            this.diskSpaceThresholdMb = diskSpaceThresholdMb;
        }
    }

    /**
     * Service discovery properties.
     */
    public static class DiscoveryProperties {

        /** Enable K8s-native service discovery. Default: true */
        private boolean enabled = true;

        /** Kubernetes namespace. Default: ftgo */
        private String namespace = "ftgo";

        /** Cluster domain suffix. Default: svc.cluster.local */
        private String clusterDomain = "svc.cluster.local";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getNamespace() {
            return namespace;
        }

        public void setNamespace(String namespace) {
            this.namespace = namespace;
        }

        public String getClusterDomain() {
            return clusterDomain;
        }

        public void setClusterDomain(String clusterDomain) {
            this.clusterDomain = clusterDomain;
        }
    }

    /**
     * Graceful shutdown properties.
     */
    public static class ShutdownProperties {

        /** Enable graceful shutdown. Default: true */
        private boolean enabled = true;

        /** Graceful shutdown timeout in seconds. Default: 30 */
        private int timeoutSeconds = 30;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getTimeoutSeconds() {
            return timeoutSeconds;
        }

        public void setTimeoutSeconds(int timeoutSeconds) {
            this.timeoutSeconds = timeoutSeconds;
        }
    }
}
