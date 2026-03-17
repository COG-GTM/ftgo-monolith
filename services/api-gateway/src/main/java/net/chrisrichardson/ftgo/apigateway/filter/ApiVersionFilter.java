package net.chrisrichardson.ftgo.apigateway.filter;

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
 * Global filter that handles API versioning via the Accept-Version header.
 * If a request arrives with an Accept-Version header (e.g., "v1"), it is forwarded
 * as an X-API-Version header to downstream services.
 *
 * Versioning strategies supported:
 * <ul>
 *   <li>URL path versioning: /api/v1/orders/** (handled by route configuration)</li>
 *   <li>Header versioning: Accept-Version: v1 (handled by this filter)</li>
 * </ul>
 */
@Component
public class ApiVersionFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(ApiVersionFilter.class);
    public static final String ACCEPT_VERSION_HEADER = "Accept-Version";
    public static final String API_VERSION_HEADER = "X-API-Version";
    public static final String DEFAULT_VERSION = "v1";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String acceptVersion = exchange.getRequest().getHeaders().getFirst(ACCEPT_VERSION_HEADER);

        String apiVersion = (acceptVersion != null && !acceptVersion.isBlank())
                ? acceptVersion
                : DEFAULT_VERSION;

        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header(API_VERSION_HEADER, apiVersion)
                .build();

        log.debug("API version resolved: {}", apiVersion);

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    @Override
    public int getOrder() {
        // Run early in the filter chain
        return -2;
    }
}
