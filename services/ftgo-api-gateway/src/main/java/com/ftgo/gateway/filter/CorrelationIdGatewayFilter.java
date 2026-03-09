package com.ftgo.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.UUID;

/**
 * Global gateway filter that generates and propagates correlation IDs.
 *
 * <p>If the incoming request includes an {@code X-Correlation-ID} header,
 * it is reused. Otherwise, a new UUID is generated. The correlation ID is:
 * <ul>
 *   <li>Added to the request headers forwarded to downstream services</li>
 *   <li>Added to the response headers returned to the client</li>
 *   <li>Stored in the exchange attributes and Reactor Context for log correlation</li>
 * </ul>
 *
 * <p>Note: This filter uses exchange attributes and Reactor Context instead of
 * SLF4J MDC because MDC is ThreadLocal-based and does not work correctly in
 * reactive/WebFlux environments where request processing hops between threads.
 */
@Component
public class CorrelationIdGatewayFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(CorrelationIdGatewayFilter.class);

    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    public static final String CORRELATION_ID_ATTR = "correlationId";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // Use existing correlation ID or generate a new one
        String correlationId = request.getHeaders().getFirst(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
            log.debug("Generated new correlation ID: {} for {} {}",
                    correlationId, request.getMethod(), request.getURI().getPath());
        } else {
            log.debug("Using existing correlation ID: {} for {} {}",
                    correlationId, request.getMethod(), request.getURI().getPath());
        }

        // Store correlation ID in exchange attributes for access by other filters
        exchange.getAttributes().put(CORRELATION_ID_ATTR, correlationId);

        // Forward correlation ID to downstream service
        ServerHttpRequest mutatedRequest = request.mutate()
                .header(CORRELATION_ID_HEADER, correlationId)
                .build();

        // Add correlation ID to response headers
        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().add(CORRELATION_ID_HEADER, correlationId);

        // Propagate correlation ID via Reactor Context for downstream reactive operators
        String finalCorrelationId = correlationId;
        return chain.filter(exchange.mutate().request(mutatedRequest).build())
                .contextWrite(Context.of(CORRELATION_ID_ATTR, finalCorrelationId));
    }

    @Override
    public int getOrder() {
        // Run before JWT validation filter
        return -200;
    }
}
