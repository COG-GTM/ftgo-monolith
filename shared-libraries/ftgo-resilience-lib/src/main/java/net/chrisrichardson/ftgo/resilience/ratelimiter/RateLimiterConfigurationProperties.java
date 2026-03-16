package net.chrisrichardson.ftgo.resilience.ratelimiter;

/**
 * Configuration properties for Resilience4j rate limiter.
 * Controls the rate of calls to downstream services.
 */
public class RateLimiterConfigurationProperties {

    private int limitForPeriod = 50;
    private long limitRefreshPeriodMillis = 1000L;
    private long timeoutDurationMillis = 500L;

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
