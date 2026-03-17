package net.chrisrichardson.ftgo.apigateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "ftgo.gateway.services")
public class GatewayServiceProperties {

    private String orderServiceUrl = "http://order-service:8080";
    private String consumerServiceUrl = "http://consumer-service:8080";
    private String restaurantServiceUrl = "http://restaurant-service:8080";
    private String courierServiceUrl = "http://courier-service:8080";

    public String getOrderServiceUrl() {
        return orderServiceUrl;
    }

    public void setOrderServiceUrl(String orderServiceUrl) {
        this.orderServiceUrl = orderServiceUrl;
    }

    public String getConsumerServiceUrl() {
        return consumerServiceUrl;
    }

    public void setConsumerServiceUrl(String consumerServiceUrl) {
        this.consumerServiceUrl = consumerServiceUrl;
    }

    public String getRestaurantServiceUrl() {
        return restaurantServiceUrl;
    }

    public void setRestaurantServiceUrl(String restaurantServiceUrl) {
        this.restaurantServiceUrl = restaurantServiceUrl;
    }

    public String getCourierServiceUrl() {
        return courierServiceUrl;
    }

    public void setCourierServiceUrl(String courierServiceUrl) {
        this.courierServiceUrl = courierServiceUrl;
    }
}
