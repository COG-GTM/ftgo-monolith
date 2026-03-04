package com.ftgo.gateway.filter;

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
 * Global gateway filter for API versioning support.
 *
 * <p>Supports two versioning strategies:
 * <ol>
 *   <li><strong>URL path versioning</strong> — {@code /api/v1/orders/**}, {@code /api/v2/orders/**}</li>
 *   <li><strong>Header versioning</strong> — {@code X-API-Version: v2}</li>
 * </ol>
 *
 * <p>If a version is specified in the URL path (e.g., /api/v2/orders), the path
 * is rewritten to remove the version prefix and the version is propagated as
 * a header to downstream services. If no version is found in the URL, the
 * X-API-Version header is used. If neither is present, defaults to v1.
 */
@Component
public class ApiVersionGatewayFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(ApiVersionGatewayFilter.class);

    public static final String API_VERSION_HEADER = "X-API-Version";
    private static final String DEFAULT_VERSION = "v1";
    private static final String VERSION_PATH_PREFIX = "/api/v";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        String version;
        String rewrittenPath = path;

        // Check for URL path versioning: /api/v{N}/...
        if (path.startsWith(VERSION_PATH_PREFIX)) {
            int versionEndIndex = path.indexOf('/', VERSION_PATH_PREFIX.length());
            if (versionEndIndex > 0) {
                version = path.substring("/api/".length(), versionEndIndex);
                // Rewrite path: /api/v1/orders -> /api/orders
                rewrittenPath = "/api" + path.substring(versionEndIndex);
            } else {
                version = path.substring("/api/".length());
                rewrittenPath = "/api";
            }
        } else {
            // Check for header-based versioning
            String headerVersion = request.getHeaders().getFirst(API_VERSION_HEADER);
            version = (headerVersion != null && !headerVersion.isBlank()) ? headerVersion : DEFAULT_VERSION;
        }

        log.debug("API version '{}' resolved for {} {} (rewritten: {})",
                version, request.getMethod(), path, rewrittenPath);

        // Mutate request with version header and potentially rewritten path
        ServerHttpRequest.Builder requestBuilder = request.mutate()
                .header(API_VERSION_HEADER, version);

        if (!rewrittenPath.equals(path)) {
            requestBuilder.path(rewrittenPath);
        }

        ServerHttpRequest mutatedRequest = requestBuilder.build();
        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    @Override
    public int getOrder() {
        // Run after correlation ID but before JWT validation
        return -180;
    }
}
