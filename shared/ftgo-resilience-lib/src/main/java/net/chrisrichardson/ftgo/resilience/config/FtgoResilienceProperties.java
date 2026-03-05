package net.chrisrichardson.ftgo.resilience.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration properties for FTGO resilience patterns.
 *
 * <p>All properties are prefixed with {@code ftgo.resilience}.
 *
 * <p>Example configuration:
 * <pre>
 * ftgo.resilience.enabled=true
 * ftgo.resilience.circuit-breaker.failure-rate-threshold=50
 * ftgo.resilience.circuit-breaker.sliding-window-size=10
 * ftgo.resilience.retry.max-attempts=3
 * ftgo.resilience.bulkhead.max-concurrent-calls=25
 * ftgo.resilience.rate-limiter.limit-for-period=50
 * </pre>
 */
@ConfigurationProperties(prefix = "ftgo.resilience")
public class FtgoResilienceProperties {

    /** Whether resilience patterns are enabled. Defaults to true. */
    private boolean enabled = true;

    /** Circuit breaker configuration. */
    private CircuitBreakerProperties circuitBreaker = new CircuitBreakerProperties();

    /** Retry configuration. */
    private RetryProperties retry = new RetryProperties();

    /** Bulkhead configuration. */
    private BulkheadProperties bulkhead = new BulkheadProperties();

    /** Rate limiter configuration. */
    private RateLimiterProperties rateLimiter = new RateLimiterProperties();

    /** Graceful shutdown configuration. */
    private GracefulShutdownProperties gracefulShutdown = new GracefulShutdownProperties();

    /** Per-service override configurations. */
    private Map<String, ServiceResilienceProperties> services = new HashMap<>();

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

    public GracefulShutdownProperties getGracefulShutdown() {
        return gracefulShutdown;
    }

    public void setGracefulShutdown(GracefulShutdownProperties gracefulShutdown) {
        this.gracefulShutdown = gracefulShutdown;
    }

    public Map<String, ServiceResilienceProperties> getServices() {
        return services;
    }

    public void setServices(Map<String, ServiceResilienceProperties> services) {
        this.services = services;
    }

    /**
     * Circuit breaker configuration properties.
     */
    public static class CircuitBreakerProperties {

        /** Failure rate threshold percentage (0-100). Default: 50. */
        private float failureRateThreshold = 50;

        /** Number of calls in the sliding window. Default: 10. */
        private int slidingWindowSize = 10;

        /** Minimum number of calls before calculating failure rate. Default: 5. */
        private int minimumNumberOfCalls = 5;

        /** Duration to stay in open state before transitioning to half-open. Default: 30s. */
        private Duration waitDurationInOpenState = Duration.ofSeconds(30);

        /** Number of permitted calls in half-open state. Default: 3. */
        private int permittedNumberOfCallsInHalfOpenState = 3;

        /** Whether to automatically transition from open to half-open. Default: true. */
        private boolean automaticTransitionFromOpenToHalfOpenEnabled = true;

        /** Sliding window type: COUNT_BASED or TIME_BASED. Default: COUNT_BASED. */
        private String slidingWindowType = "COUNT_BASED";

        public float getFailureRateThreshold() {
            return failureRateThreshold;
        }

        public void setFailureRateThreshold(float failureRateThreshold) {
            this.failureRateThreshold = failureRateThreshold;
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

        public Duration getWaitDurationInOpenState() {
            return waitDurationInOpenState;
        }

        public void setWaitDurationInOpenState(Duration waitDurationInOpenState) {
            this.waitDurationInOpenState = waitDurationInOpenState;
        }

        public int getPermittedNumberOfCallsInHalfOpenState() {
            return permittedNumberOfCallsInHalfOpenState;
        }

        public void setPermittedNumberOfCallsInHalfOpenState(int permittedNumberOfCallsInHalfOpenState) {
            this.permittedNumberOfCallsInHalfOpenState = permittedNumberOfCallsInHalfOpenState;
        }

        public boolean isAutomaticTransitionFromOpenToHalfOpenEnabled() {
            return automaticTransitionFromOpenToHalfOpenEnabled;
        }

        public void setAutomaticTransitionFromOpenToHalfOpenEnabled(boolean automaticTransitionFromOpenToHalfOpenEnabled) {
            this.automaticTransitionFromOpenToHalfOpenEnabled = automaticTransitionFromOpenToHalfOpenEnabled;
        }

        public String getSlidingWindowType() {
            return slidingWindowType;
        }

        public void setSlidingWindowType(String slidingWindowType) {
            this.slidingWindowType = slidingWindowType;
        }
    }

    /**
     * Retry configuration properties.
     */
    public static class RetryProperties {

        /** Maximum number of retry attempts. Default: 3. */
        private int maxAttempts = 3;

        /** Initial wait duration before first retry. Default: 1s. */
        private Duration waitDuration = Duration.ofSeconds(1);

        /** Multiplier for exponential backoff. Default: 2.0. */
        private double multiplier = 2.0;

        /** Whether to use exponential backoff. Default: true. */
        private boolean exponentialBackoff = true;

        public int getMaxAttempts() {
            return maxAttempts;
        }

        public void setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }

        public Duration getWaitDuration() {
            return waitDuration;
        }

        public void setWaitDuration(Duration waitDuration) {
            this.waitDuration = waitDuration;
        }

        public double getMultiplier() {
            return multiplier;
        }

        public void setMultiplier(double multiplier) {
            this.multiplier = multiplier;
        }

        public boolean isExponentialBackoff() {
            return exponentialBackoff;
        }

        public void setExponentialBackoff(boolean exponentialBackoff) {
            this.exponentialBackoff = exponentialBackoff;
        }
    }

    /**
     * Bulkhead configuration properties.
     */
    public static class BulkheadProperties {

        /** Maximum number of concurrent calls. Default: 25. */
        private int maxConcurrentCalls = 25;

        /** Maximum wait duration for a permit. Default: 0 (fail immediately). */
        private Duration maxWaitDuration = Duration.ZERO;

        public int getMaxConcurrentCalls() {
            return maxConcurrentCalls;
        }

        public void setMaxConcurrentCalls(int maxConcurrentCalls) {
            this.maxConcurrentCalls = maxConcurrentCalls;
        }

        public Duration getMaxWaitDuration() {
            return maxWaitDuration;
        }

        public void setMaxWaitDuration(Duration maxWaitDuration) {
            this.maxWaitDuration = maxWaitDuration;
        }
    }

    /**
     * Rate limiter configuration properties.
     */
    public static class RateLimiterProperties {

        /** Number of permissions available during one limit refresh period. Default: 50. */
        private int limitForPeriod = 50;

        /** Duration of one limit refresh period. Default: 1s. */
        private Duration limitRefreshPeriod = Duration.ofSeconds(1);

        /** Maximum wait duration for a permission. Default: 500ms. */
        private Duration timeoutDuration = Duration.ofMillis(500);

        public int getLimitForPeriod() {
            return limitForPeriod;
        }

        public void setLimitForPeriod(int limitForPeriod) {
            this.limitForPeriod = limitForPeriod;
        }

        public Duration getLimitRefreshPeriod() {
            return limitRefreshPeriod;
        }

        public void setLimitRefreshPeriod(Duration limitRefreshPeriod) {
            this.limitRefreshPeriod = limitRefreshPeriod;
        }

        public Duration getTimeoutDuration() {
            return timeoutDuration;
        }

        public void setTimeoutDuration(Duration timeoutDuration) {
            this.timeoutDuration = timeoutDuration;
        }
    }

    /**
     * Graceful shutdown configuration properties.
     */
    public static class GracefulShutdownProperties {

        /** Timeout for graceful shutdown. Default: 30s. */
        private Duration timeout = Duration.ofSeconds(30);

        /** Whether to await termination of active requests. Default: true. */
        private boolean awaitTermination = true;

        public Duration getTimeout() {
            return timeout;
        }

        public void setTimeout(Duration timeout) {
            this.timeout = timeout;
        }

        public boolean isAwaitTermination() {
            return awaitTermination;
        }

        public void setAwaitTermination(boolean awaitTermination) {
            this.awaitTermination = awaitTermination;
        }
    }

    /**
     * Per-service resilience override properties.
     */
    public static class ServiceResilienceProperties {

        private CircuitBreakerProperties circuitBreaker;
        private RetryProperties retry;
        private BulkheadProperties bulkhead;
        private RateLimiterProperties rateLimiter;

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
    }
}
