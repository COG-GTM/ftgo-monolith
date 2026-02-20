package com.ftgo.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class RequestLoggingGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingGlobalFilter.class);
    private static final String START_TIME_ATTR = "gateway.request.startTime";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        String correlationId = request.getHeaders().getFirst(CorrelationIdGlobalFilter.CORRELATION_ID_HEADER);
        HttpMethod method = request.getMethod();
        String path = request.getURI().getPath();
        String remoteAddress = request.getRemoteAddress() != null
                ? request.getRemoteAddress().getAddress().getHostAddress() : "unknown";

        log.info("Incoming request: method={}, path={}, remoteAddress={}, correlationId={}",
                method, path, remoteAddress, correlationId);

        exchange.getAttributes().put(START_TIME_ATTR, System.currentTimeMillis());

        return chain.filter(exchange)
                .then(Mono.fromRunnable(() -> {
                    Long startTime = exchange.getAttribute(START_TIME_ATTR);
                    long duration = startTime != null ? System.currentTimeMillis() - startTime : -1;

                    ServerHttpResponse response = exchange.getResponse();
                    int statusCode = response.getStatusCode() != null
                            ? response.getStatusCode().value() : 0;

                    log.info("Outgoing response: method={}, path={}, status={}, duration={}ms, correlationId={}",
                            method, path, statusCode, duration, correlationId);
                }));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}
