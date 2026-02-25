package com.ftgo.gateway.filter;

import com.ftgo.gateway.config.GatewayProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Global filter that implements API versioning via HTTP header.
 * <p>
 * If the incoming request does not include the version header
 * ({@code X-API-Version} by default), this filter injects the
 * configured default version. Downstream services can inspect
 * this header to implement version-specific behavior.
 * </p>
 * <p>
 * API versioning is also supported via URL path prefixes
 * (e.g., {@code /v1/api/orders/**}), which is handled in
 * {@link com.ftgo.gateway.config.GatewayRouteConfiguration}.
 * </p>
 */
@Component
public class ApiVersionGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(ApiVersionGlobalFilter.class);

    private final GatewayProperties gatewayProperties;

    public ApiVersionGlobalFilter(GatewayProperties gatewayProperties) {
        this.gatewayProperties = gatewayProperties;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!gatewayProperties.getVersioning().isEnabled()) {
            return chain.filter(exchange);
        }

        String headerName = gatewayProperties.getVersioning().getHeaderName();
        String existingVersion = exchange.getRequest().getHeaders().getFirst(headerName);

        if (existingVersion == null || existingVersion.isBlank()) {
            String defaultVersion = gatewayProperties.getVersioning().getDefaultVersion();
            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .header(headerName, defaultVersion)
                    .build();
            log.debug("Set default API version header: {}={}", headerName, defaultVersion);
            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        }

        log.debug("API version header present: {}={}", headerName, existingVersion);
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        // Run after correlation ID and logging filters
        return Ordered.HIGHEST_PRECEDENCE + 3;
    }
}
