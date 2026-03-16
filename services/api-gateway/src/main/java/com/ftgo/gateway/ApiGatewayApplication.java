package com.ftgo.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * FTGO API Gateway - Single entry point for all microservices.
 *
 * <p>Built on Spring Cloud Gateway (reactive/Netty-based), this gateway provides:
 * <ul>
 *   <li>Route-based request forwarding to downstream microservices</li>
 *   <li>JWT token validation</li>
 *   <li>In-memory rate limiting</li>
 *   <li>Circuit breaker patterns (Resilience4j)</li>
 *   <li>CORS configuration</li>
 *   <li>Correlation ID propagation for distributed tracing</li>
 * </ul>
 */
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
