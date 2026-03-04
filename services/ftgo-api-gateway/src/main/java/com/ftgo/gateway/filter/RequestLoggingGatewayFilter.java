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
 * Global gateway filter for request/response logging.
 *
 * <p>Logs incoming requests with method, path, and client IP,
 * and outgoing responses with status code and duration.
 */
@Component
public class RequestLoggingGatewayFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingGatewayFilter.class);
    private static final String REQUEST_START_TIME = "requestStartTime";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        long startTime = System.currentTimeMillis();
        exchange.getAttributes().put(REQUEST_START_TIME, startTime);

        String method = request.getMethod() != null ? request.getMethod().name() : "UNKNOWN";
        String path = request.getURI().getPath();
        String clientIp = request.getRemoteAddress() != null
                ? request.getRemoteAddress().getAddress().getHostAddress()
                : "unknown";

        String correlationId = request.getHeaders().getFirst(CorrelationIdGatewayFilter.CORRELATION_ID_HEADER);

        log.info("Gateway Request: {} {} from {} [correlationId={}]",
                method, path, clientIp, correlationId);

        return chain.filter(exchange)
                .doFinally(signalType -> {
                    Long start = exchange.getAttribute(REQUEST_START_TIME);
                    long duration = (start != null) ? System.currentTimeMillis() - start : -1;

                    ServerHttpResponse response = exchange.getResponse();
                    int statusCode = response.getStatusCode() != null
                            ? response.getStatusCode().value()
                            : 0;

                    log.info("Gateway Response: {} {} -> {} ({}ms) [correlationId={}]",
                            method, path, statusCode, duration, correlationId);
                });
    }

    @Override
    public int getOrder() {
        // Run after correlation ID filter but before JWT filter
        return -150;
    }
}
