package net.chrisrichardson.ftgo.resilience.bulkhead;

/**
 * Configuration properties for Resilience4j bulkhead (thread pool isolation).
 * Controls concurrency limits for downstream service calls.
 */
public class BulkheadConfigurationProperties {

    private int maxConcurrentCalls = 25;
    private long maxWaitTimeMillis = 500L;

    public int getMaxConcurrentCalls() {
        return maxConcurrentCalls;
    }

    public void setMaxConcurrentCalls(int maxConcurrentCalls) {
        this.maxConcurrentCalls = maxConcurrentCalls;
    }

    public long getMaxWaitTimeMillis() {
        return maxWaitTimeMillis;
    }

    public void setMaxWaitTimeMillis(long maxWaitTimeMillis) {
        this.maxWaitTimeMillis = maxWaitTimeMillis;
    }
}
