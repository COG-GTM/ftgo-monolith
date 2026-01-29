package net.chrisrichardson.ftgo.orderservice.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class CourierServiceProxyConfiguration {

    @Value("${courier.service.url:http://localhost:8082}")
    private String courierServiceUrl;

    @Value("${courier.service.connect-timeout:5000}")
    private int connectTimeout;

    @Value("${courier.service.read-timeout:5000}")
    private int readTimeout;

    @Bean
    public RestTemplate courierServiceRestTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(connectTimeout)
                .setReadTimeout(readTimeout)
                .build();
    }

    @Bean
    public CourierServiceProxy courierServiceProxy(RestTemplate courierServiceRestTemplate) {
        return new CourierServiceProxy(courierServiceRestTemplate, courierServiceUrl);
    }
}
