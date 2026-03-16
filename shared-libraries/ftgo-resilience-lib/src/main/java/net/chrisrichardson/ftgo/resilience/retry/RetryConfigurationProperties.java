package net.chrisrichardson.ftgo.resilience.retry;

/**
 * Configuration properties for Resilience4j retry with exponential backoff.
 * Provides sensible defaults for FTGO microservices inter-service communication.
 */
public class RetryConfigurationProperties {

    private int maxAttempts = 3;
    private long initialIntervalMillis = 500L;
    private double multiplier = 2.0;
    private long maxIntervalMillis = 5000L;

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
