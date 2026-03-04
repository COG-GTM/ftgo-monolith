package com.ftgo.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Global gateway filter that generates and propagates correlation IDs.
 *
 * <p>If the incoming request includes an {@code X-Correlation-ID} header,
 * it is reused. Otherwise, a new UUID is generated. The correlation ID is:
 * <ul>
 *   <li>Added to the request headers forwarded to downstream services</li>
 *   <li>Added to the response headers returned to the client</li>
 *   <li>Placed in the SLF4J MDC for log correlation</li>
 * </ul>
 */
@Component
public class CorrelationIdGatewayFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(CorrelationIdGatewayFilter.class);

    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String MDC_CORRELATION_ID = "correlationId";

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

        // Add correlation ID to MDC for logging
        MDC.put(MDC_CORRELATION_ID, correlationId);

        // Forward correlation ID to downstream service
        ServerHttpRequest mutatedRequest = request.mutate()
                .header(CORRELATION_ID_HEADER, correlationId)
                .build();

        // Add correlation ID to response headers
        String finalCorrelationId = correlationId;
        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().add(CORRELATION_ID_HEADER, finalCorrelationId);

        return chain.filter(exchange.mutate().request(mutatedRequest).build())
                .doFinally(signalType -> MDC.remove(MDC_CORRELATION_ID));
    }

    @Override
    public int getOrder() {
        // Run before JWT validation filter
        return -200;
    }
}
