package com.ftgo.gateway.filter;

import com.ftgo.gateway.config.GatewayProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Gateway filter that validates JWT tokens on incoming requests.
 *
 * <p>Extracts the {@code Authorization: Bearer <token>} header, parses
 * the JWT using the configured secret, and forwards validated claims
 * (user ID, roles) as request headers to downstream services.
 *
 * <p>Requests without a token or with an invalid/expired token receive
 * a {@code 401 Unauthorized} response. The filter can be disabled via
 * {@code gateway.jwt.enabled=false} for development environments.
 */
@Component
public class JwtValidationFilter implements GatewayFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(JwtValidationFilter.class);

    private final GatewayProperties gatewayProperties;

    public JwtValidationFilter(GatewayProperties gatewayProperties) {
        this.gatewayProperties = gatewayProperties;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!gatewayProperties.getJwt().isEnabled()) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders()
                .getFirst(gatewayProperties.getJwt().getHeaderName());

        if (authHeader == null || !authHeader.startsWith(gatewayProperties.getJwt().getTokenPrefix())) {
            log.warn("Missing or invalid Authorization header for request: {} {}",
                    exchange.getRequest().getMethod(),
                    exchange.getRequest().getURI().getPath());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(gatewayProperties.getJwt().getTokenPrefix().length());

        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(gatewayProperties.getJwt().getSecret().getBytes())
                    .parseClaimsJws(token)
                    .getBody();

            String userId = claims.getSubject();
            String roles = claims.get("roles", String.class);

            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .header("X-User-Id", userId != null ? userId : "")
                    .header("X-User-Roles", roles != null ? roles : "")
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch (ExpiredJwtException e) {
            log.warn("Expired JWT token for request: {}", exchange.getRequest().getURI().getPath());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        } catch (MalformedJwtException | UnsupportedJwtException | SignatureException | IllegalArgumentException e) {
            log.warn("Invalid JWT token for request: {} - {}",
                    exchange.getRequest().getURI().getPath(), e.getMessage());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 2;
    }
}
