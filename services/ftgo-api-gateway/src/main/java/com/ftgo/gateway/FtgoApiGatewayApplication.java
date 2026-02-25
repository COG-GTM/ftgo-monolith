package com.ftgo.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * FTGO API Gateway - Single entry point for all FTGO microservices.
 * <p>
 * Built on Spring Cloud Gateway (reactive/WebFlux), this gateway handles:
 * <ul>
 *   <li>Request routing to downstream microservices</li>
 *   <li>JWT authentication and claims forwarding</li>
 *   <li>Rate limiting (Redis-backed with in-memory fallback)</li>
 *   <li>Circuit breaking via Resilience4j</li>
 *   <li>CORS configuration</li>
 *   <li>Correlation ID propagation</li>
 *   <li>Request/response logging</li>
 *   <li>API versioning</li>
 * </ul>
 */
@SpringBootApplication
public class FtgoApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(FtgoApiGatewayApplication.class, args);
    }
}
