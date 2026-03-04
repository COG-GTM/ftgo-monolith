package com.ftgo.gateway.ratelimit;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Key resolver for rate limiting that identifies clients by API key or IP address.
 *
 * <p>Resolution strategy (in order of priority):
 * <ol>
 *   <li>X-API-Key header — for authenticated API clients</li>
 *   <li>X-User-ID header — for JWT-authenticated users (set by JwtValidationGatewayFilter)</li>
 *   <li>Client IP address — fallback for unauthenticated requests</li>
 * </ol>
 */
@Component("apiKeyKeyResolver")
public class ApiKeyKeyResolver implements KeyResolver {

    private static final String API_KEY_HEADER = "X-API-Key";
    private static final String USER_ID_HEADER = "X-User-ID";

    @Override
    public Mono<String> resolve(ServerWebExchange exchange) {
        // Priority 1: API Key
        String apiKey = exchange.getRequest().getHeaders().getFirst(API_KEY_HEADER);
        if (apiKey != null && !apiKey.isBlank()) {
            return Mono.just("apikey:" + apiKey);
        }

        // Priority 2: Authenticated user ID (from JWT validation)
        String userId = exchange.getRequest().getHeaders().getFirst(USER_ID_HEADER);
        if (userId != null && !userId.isBlank()) {
            return Mono.just("user:" + userId);
        }

        // Priority 3: Client IP address
        if (exchange.getRequest().getRemoteAddress() != null) {
            String ip = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
            return Mono.just("ip:" + ip);
        }

        return Mono.just("anonymous");
    }
}
