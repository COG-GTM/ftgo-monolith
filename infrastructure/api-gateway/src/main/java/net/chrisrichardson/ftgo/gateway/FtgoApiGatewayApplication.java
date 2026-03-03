package net.chrisrichardson.ftgo.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * FTGO API Gateway - Single entry point for all FTGO microservices.
 *
 * <p>Built on Spring Cloud Gateway, this service handles:
 * <ul>
 *   <li>Request routing to downstream microservices</li>
 *   <li>JWT-based authentication and authorization</li>
 *   <li>Redis-backed rate limiting per client/API key</li>
 *   <li>Circuit breaker patterns (Resilience4j)</li>
 *   <li>CORS configuration</li>
 *   <li>Correlation ID propagation</li>
 *   <li>API versioning via URL path</li>
 * </ul>
 */
@SpringBootApplication
public class FtgoApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(FtgoApiGatewayApplication.class, args);
    }
}
