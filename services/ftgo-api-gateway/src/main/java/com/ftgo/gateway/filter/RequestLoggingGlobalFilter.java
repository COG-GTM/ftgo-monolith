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

/**
 * Global filter for request/response logging.
 * <p>
 * Logs the HTTP method, URI, correlation ID, and response status code
 * for every request passing through the gateway. This provides a
 * centralized audit trail of all API calls.
 * </p>
 */
@Component
public class RequestLoggingGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingGlobalFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String correlationId = request.getHeaders().getFirst(CorrelationIdGlobalFilter.CORRELATION_ID_HEADER);
        String method = request.getMethod().name();
        String path = request.getURI().getPath();
        String clientIp = request.getRemoteAddress() != null
                ? request.getRemoteAddress().getAddress().getHostAddress()
                : "unknown";

        long startTime = System.currentTimeMillis();

        log.info("Gateway request: {} {} from {} [correlationId={}]", method, path, clientIp, correlationId);

        return chain.filter(exchange)
                .then(Mono.fromRunnable(() -> {
                    ServerHttpResponse response = exchange.getResponse();
                    long duration = System.currentTimeMillis() - startTime;
                    int statusCode = response.getStatusCode() != null
                            ? response.getStatusCode().value()
                            : 0;
                    log.info("Gateway response: {} {} -> {} ({}ms) [correlationId={}]",
                            method, path, statusCode, duration, correlationId);
                }));
    }

    @Override
    public int getOrder() {
        // Run after correlation ID filter
        return Ordered.HIGHEST_PRECEDENCE + 2;
    }
}
