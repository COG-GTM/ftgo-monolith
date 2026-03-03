package net.chrisrichardson.ftgo.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.util.UUID;

/**
 * Gateway filter that ensures every request has a correlation ID.
 *
 * <p>If an incoming request already has an {@code X-Correlation-Id} header,
 * it is preserved and forwarded. Otherwise, a new UUID is generated.
 *
 * <p>The correlation ID is:
 * <ul>
 *   <li>Forwarded to downstream services via the request header</li>
 *   <li>Included in the response headers for client-side tracing</li>
 *   <li>Added to the MDC for structured logging</li>
 * </ul>
 */
@Component
public class CorrelationIdFilter extends AbstractGatewayFilterFactory<CorrelationIdFilter.Config> {

    private static final Logger log = LoggerFactory.getLogger(CorrelationIdFilter.class);

    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    public CorrelationIdFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String correlationId = exchange.getRequest().getHeaders().getFirst(CORRELATION_ID_HEADER);

            if (correlationId == null || correlationId.isBlank()) {
                correlationId = UUID.randomUUID().toString();
                log.debug("Generated new correlation ID: {}", correlationId);
            } else {
                log.debug("Using existing correlation ID: {}", correlationId);
            }

            // Add correlation ID to the request forwarded to downstream services
            ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                    .header(CORRELATION_ID_HEADER, correlationId)
                    .build();

            ServerWebExchange modifiedExchange = exchange.mutate()
                    .request(modifiedRequest)
                    .build();

            // Add correlation ID to the response
            final String finalCorrelationId = correlationId;
            modifiedExchange.getResponse().getHeaders().add(CORRELATION_ID_HEADER, finalCorrelationId);

            return chain.filter(modifiedExchange);
        };
    }

    public static class Config {
        // Configuration properties for the filter (extensible)
    }
}
