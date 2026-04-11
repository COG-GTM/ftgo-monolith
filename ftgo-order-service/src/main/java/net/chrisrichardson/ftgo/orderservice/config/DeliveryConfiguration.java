package net.chrisrichardson.ftgo.orderservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@ConfigurationProperties(prefix = "ftgo.delivery")
public class DeliveryConfiguration {
    private Duration deliveryWindow = Duration.ofMinutes(30);
    
    public Duration getDeliveryWindow() {
        return deliveryWindow;
    }
    
    public void setDeliveryWindow(Duration deliveryWindow) {
        this.deliveryWindow = deliveryWindow;
    }
}
