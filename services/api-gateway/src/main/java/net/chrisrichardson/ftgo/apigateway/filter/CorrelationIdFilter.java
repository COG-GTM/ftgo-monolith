package net.chrisrichardson.ftgo.apigateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Gateway filter that ensures every request has a correlation ID.
 * If the incoming request already has an X-Correlation-ID header, it is preserved.
 * Otherwise, a new UUID is generated and added to both the request and response.
 */
@Component
public class CorrelationIdFilter extends AbstractGatewayFilterFactory<CorrelationIdFilter.Config> {

    private static final Logger log = LoggerFactory.getLogger(CorrelationIdFilter.class);
    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";

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

            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .header(CORRELATION_ID_HEADER, correlationId)
                    .build();

            String finalCorrelationId = correlationId;
            return chain.filter(exchange.mutate().request(mutatedRequest).build())
                    .then(reactor.core.publisher.Mono.fromRunnable(() ->
                            exchange.getResponse().getHeaders()
                                    .addIfAbsent(CORRELATION_ID_HEADER, finalCorrelationId)));
        };
    }

    public static class Config {
        // Configuration properties can be added here if needed
    }
}
