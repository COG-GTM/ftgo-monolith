package com.ftgo.resilience.health;

import com.ftgo.resilience.config.ResilienceProperties;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

public class MessagingHealthIndicator implements HealthIndicator {

    private final ResilienceProperties properties;
    private volatile boolean messagingAvailable = true;
    private volatile String lastError;
    private volatile long lastCheckTimestamp;

    public MessagingHealthIndicator(ResilienceProperties properties) {
        this.properties = properties;
    }

    @Override
    public Health health() {
        lastCheckTimestamp = System.currentTimeMillis();

        if (messagingAvailable) {
            return Health.up()
                    .withDetail("messaging", "available")
                    .withDetail("lastChecked", lastCheckTimestamp)
                    .withDetail("timeoutMs", properties.getHealthCheck().getTimeoutMs())
                    .build();
        }

        Health.Builder builder = Health.down()
                .withDetail("messaging", "unavailable")
                .withDetail("lastChecked", lastCheckTimestamp);

        if (lastError != null) {
            builder.withDetail("error", lastError);
        }

        return builder.build();
    }

    public void markAvailable() {
        this.messagingAvailable = true;
        this.lastError = null;
    }

    public void markUnavailable(String error) {
        this.messagingAvailable = false;
        this.lastError = error;
    }

    public boolean isMessagingAvailable() {
        return messagingAvailable;
    }
}
