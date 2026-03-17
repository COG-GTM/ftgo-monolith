package net.chrisrichardson.ftgo.apigateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Gateway filter that logs request and response details for observability.
 * Logs the HTTP method, URI, correlation ID on request, and status code on response.
 */
@Component
public class RequestLoggingFilter extends AbstractGatewayFilterFactory<RequestLoggingFilter.Config> {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    public RequestLoggingFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            long startTime = System.currentTimeMillis();
            String method = exchange.getRequest().getMethod().name();
            String uri = exchange.getRequest().getURI().toString();
            String correlationId = exchange.getRequest().getHeaders()
                    .getFirst(CorrelationIdFilter.CORRELATION_ID_HEADER);

            log.info("Gateway Request: method={}, uri={}, correlationId={}", method, uri, correlationId);

            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                long duration = System.currentTimeMillis() - startTime;
                int statusCode = exchange.getResponse().getStatusCode() != null
                        ? exchange.getResponse().getStatusCode().value()
                        : 0;
                log.info("Gateway Response: method={}, uri={}, status={}, duration={}ms, correlationId={}",
                        method, uri, statusCode, duration, correlationId);
            }));
        };
    }

    public static class Config {
        // Configuration properties can be added here if needed
    }
}
