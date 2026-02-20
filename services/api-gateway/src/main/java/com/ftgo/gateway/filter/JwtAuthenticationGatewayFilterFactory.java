package com.ftgo.gateway.filter;

import com.ftgo.jwt.JwtProperties;
import com.ftgo.jwt.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class JwtAuthenticationGatewayFilterFactory
        extends AbstractGatewayFilterFactory<JwtAuthenticationGatewayFilterFactory.Config> {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationGatewayFilterFactory.class);

    private final JwtTokenProvider tokenProvider;
    private final JwtProperties jwtProperties;

    public JwtAuthenticationGatewayFilterFactory(JwtTokenProvider tokenProvider, JwtProperties jwtProperties) {
        super(Config.class);
        this.tokenProvider = tokenProvider;
        this.jwtProperties = jwtProperties;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String token = extractToken(exchange);

            if (token == null || token.isEmpty()) {
                log.debug("No JWT token found in request");
                return unauthorized(exchange, "Missing authentication token");
            }

            if (!tokenProvider.validateToken(token)) {
                log.debug("Invalid JWT token");
                return unauthorized(exchange, "Invalid authentication token");
            }

            String subject = tokenProvider.getSubject(token);
            List<String> roles = tokenProvider.getRoles(token);

            if (!config.getRequiredRoles().isEmpty()) {
                boolean hasRequiredRole = config.getRequiredRoles().stream()
                        .anyMatch(roles::contains);
                if (!hasRequiredRole) {
                    log.debug("User {} lacks required roles: {}", subject, config.getRequiredRoles());
                    return forbidden(exchange, "Insufficient permissions");
                }
            }

            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .header("X-User-Id", subject)
                    .header("X-User-Roles", String.join(",", roles))
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        };
    }

    private String extractToken(ServerWebExchange exchange) {
        String bearerToken = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (bearerToken != null && bearerToken.startsWith(jwtProperties.getPrefix())) {
            return bearerToken.substring(jwtProperties.getPrefix().length());
        }
        return null;
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().add("X-Gateway-Error", message);
        return exchange.getResponse().setComplete();
    }

    private Mono<Void> forbidden(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        exchange.getResponse().getHeaders().add("X-Gateway-Error", message);
        return exchange.getResponse().setComplete();
    }

    public static class Config {

        private List<String> requiredRoles = new ArrayList<>();

        public List<String> getRequiredRoles() {
            return Collections.unmodifiableList(requiredRoles);
        }

        public void setRequiredRoles(List<String> requiredRoles) {
            this.requiredRoles = requiredRoles != null ? new ArrayList<>(requiredRoles) : new ArrayList<>();
        }
    }
}
