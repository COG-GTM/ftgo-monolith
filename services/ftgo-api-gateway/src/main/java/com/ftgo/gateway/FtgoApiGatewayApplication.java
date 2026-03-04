package com.ftgo.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * FTGO API Gateway — single entry point for all FTGO microservices.
 *
 * <p>This Spring Cloud Gateway application provides:
 * <ul>
 *   <li>Request routing to downstream microservices</li>
 *   <li>JWT validation and claims forwarding</li>
 *   <li>Rate limiting (Redis-backed)</li>
 *   <li>Circuit breaker patterns (Resilience4j)</li>
 *   <li>CORS configuration</li>
 *   <li>SSL/TLS termination</li>
 *   <li>Request/response logging with correlation ID propagation</li>
 *   <li>API versioning via URL path and header</li>
 * </ul>
 */
@SpringBootApplication
public class FtgoApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(FtgoApiGatewayApplication.class, args);
    }
}
