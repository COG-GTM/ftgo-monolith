package net.chrisrichardson.ftgo.resilience.circuitbreaker;

/**
 * Configuration properties for Resilience4j circuit breaker instances.
 * Provides sensible defaults for FTGO microservices inter-service communication.
 */
public class CircuitBreakerConfigurationProperties {

    private float failureRateThreshold = 50.0f;
    private int ringBufferSizeInClosedState = 10;
    private int ringBufferSizeInHalfOpenState = 5;
    private long waitDurationInOpenStateMillis = 30000L;

    public float getFailureRateThreshold() {
        return failureRateThreshold;
    }

    public void setFailureRateThreshold(float failureRateThreshold) {
        this.failureRateThreshold = failureRateThreshold;
    }

    public int getRingBufferSizeInClosedState() {
        return ringBufferSizeInClosedState;
    }

    public void setRingBufferSizeInClosedState(int ringBufferSizeInClosedState) {
        this.ringBufferSizeInClosedState = ringBufferSizeInClosedState;
    }

    public int getRingBufferSizeInHalfOpenState() {
        return ringBufferSizeInHalfOpenState;
    }

    public void setRingBufferSizeInHalfOpenState(int ringBufferSizeInHalfOpenState) {
        this.ringBufferSizeInHalfOpenState = ringBufferSizeInHalfOpenState;
    }

    public long getWaitDurationInOpenStateMillis() {
        return waitDurationInOpenStateMillis;
    }

    public void setWaitDurationInOpenStateMillis(long waitDurationInOpenStateMillis) {
        this.waitDurationInOpenStateMillis = waitDurationInOpenStateMillis;
    }

}
