package com.ftgo.gateway.filter;

import com.ftgo.gateway.config.GatewayProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Spring Cloud Gateway filter factory for JWT authentication.
 * <p>
 * This filter validates JWT tokens at the gateway level and forwards
 * extracted claims as HTTP headers to downstream services. This avoids
 * the need for each downstream service to independently parse the JWT.
 * </p>
 * <p>
 * Headers forwarded to downstream services:
 * <ul>
 *   <li>{@code X-Auth-UserId} - Numeric user ID</li>
 *   <li>{@code X-Auth-Username} - Username (subject)</li>
 *   <li>{@code X-Auth-Roles} - Comma-separated roles</li>
 *   <li>{@code X-Auth-Permissions} - Comma-separated permissions</li>
 * </ul>
 * The original {@code Authorization} header is also forwarded so
 * downstream services can optionally re-validate.
 * </p>
 *
 * @see GatewayProperties.Jwt
 */
@Component
public class JwtAuthenticationGatewayFilterFactory
        extends AbstractGatewayFilterFactory<JwtAuthenticationGatewayFilterFactory.Config> {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationGatewayFilterFactory.class);

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String CLAIM_ROLES = "roles";
    private static final String CLAIM_PERMISSIONS = "permissions";
    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_TOKEN_TYPE = "type";
    private static final String TOKEN_TYPE_ACCESS = "access";

    private final GatewayProperties gatewayProperties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private volatile SecretKey signingKey;

    public JwtAuthenticationGatewayFilterFactory(GatewayProperties gatewayProperties) {
        super(Config.class);
        this.gatewayProperties = gatewayProperties;
    }

    private SecretKey getSigningKey() {
        if (signingKey == null) {
            synchronized (this) {
                if (signingKey == null) {
                    String secret = gatewayProperties.getJwt().getSecret();
                    if (secret != null && !secret.isBlank()) {
                        signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
                    }
                }
            }
        }
        return signingKey;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            // Skip JWT validation if disabled
            if (!gatewayProperties.getJwt().isEnabled()) {
                return chain.filter(exchange);
            }

            // Skip JWT validation for excluded paths
            String path = exchange.getRequest().getURI().getPath();
            for (String excludedPath : gatewayProperties.getJwt().getExcludedPaths()) {
                if (pathMatcher.match(excludedPath, path)) {
                    return chain.filter(exchange);
                }
            }

            // Extract token from Authorization header
            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
                return unauthorizedResponse(exchange.getResponse(), "Missing or invalid Authorization header");
            }

            String token = authHeader.substring(BEARER_PREFIX.length());

            try {
                SecretKey key = getSigningKey();
                if (key == null) {
                    log.error("JWT secret not configured; cannot validate token");
                    return unauthorizedResponse(exchange.getResponse(), "JWT validation unavailable");
                }

                Claims claims = Jwts.parser()
                        .verifyWith(key)
                        .requireIssuer(gatewayProperties.getJwt().getIssuer())
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();

                // Verify it's an access token
                String tokenType = claims.get(CLAIM_TOKEN_TYPE, String.class);
                if (!TOKEN_TYPE_ACCESS.equals(tokenType)) {
                    return unauthorizedResponse(exchange.getResponse(), "Invalid token type");
                }

                // Extract claims
                String username = claims.getSubject();
                Object userIdObj = claims.get(CLAIM_USER_ID);
                String userId = userIdObj instanceof Number ? String.valueOf(((Number) userIdObj).longValue()) : "";

                @SuppressWarnings("unchecked")
                List<String> roles = claims.get(CLAIM_ROLES) instanceof List<?> list
                        ? ((List<Object>) list).stream().map(Object::toString).collect(Collectors.toList())
                        : Collections.emptyList();

                @SuppressWarnings("unchecked")
                List<String> permissions = claims.get(CLAIM_PERMISSIONS) instanceof List<?> list
                        ? ((List<Object>) list).stream().map(Object::toString).collect(Collectors.toList())
                        : Collections.emptyList();

                // Forward claims as headers to downstream services
                ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                        .header("X-Auth-UserId", userId)
                        .header("X-Auth-Username", username)
                        .header("X-Auth-Roles", String.join(",", roles))
                        .header("X-Auth-Permissions", String.join(",", permissions))
                        .build();

                log.debug("JWT validated for user '{}' (id={}) on path {}", username, userId, path);

                return chain.filter(exchange.mutate().request(mutatedRequest).build());

            } catch (ExpiredJwtException ex) {
                log.debug("JWT token expired: {}", ex.getMessage());
                return unauthorizedResponse(exchange.getResponse(), "Token expired");
            } catch (JwtException ex) {
                log.debug("Invalid JWT token: {}", ex.getMessage());
                return unauthorizedResponse(exchange.getResponse(), "Invalid token");
            } catch (IllegalArgumentException ex) {
                log.debug("JWT token is blank or null: {}", ex.getMessage());
                return unauthorizedResponse(exchange.getResponse(), "Invalid token");
            }
        };
    }

    private Mono<Void> unauthorizedResponse(ServerHttpResponse response, String message) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = String.format("{\"error\":\"Unauthorized\",\"message\":\"%s\",\"status\":401}", message);
        return response.writeWith(Mono.just(
                response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8))));
    }

    /**
     * Configuration class for the JWT filter. Currently uses gateway-level
     * properties but can be extended for per-route overrides.
     */
    public static class Config {
        // Configuration can be extended for per-route JWT settings
    }
}
