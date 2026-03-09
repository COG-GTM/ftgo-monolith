package com.ftgo.gateway.filter;

import com.ftgo.gateway.config.GatewayJwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

/**
 * Global gateway filter that validates JWT tokens on incoming requests.
 *
 * <p>This filter:
 * <ol>
 *   <li>Extracts the Bearer token from the Authorization header</li>
 *   <li>Validates the JWT signature, expiration, and issuer</li>
 *   <li>Forwards validated claims as headers to downstream services:
 *       {@code X-User-ID}, {@code X-Username}, {@code X-User-Roles}</li>
 *   <li>Rejects requests with invalid/expired tokens with 401</li>
 *   <li>Skips validation for public endpoints (actuator, health, etc.)</li>
 * </ol>
 */
@Component
public class JwtValidationGatewayFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(JwtValidationGatewayFilter.class);

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String CLAIM_USERNAME = "username";
    private static final String CLAIM_ROLES = "roles";
    private static final String CLAIM_TOKEN_TYPE = "type";
    private static final String TOKEN_TYPE_ACCESS = "access";

    private static final String HEADER_USER_ID = "X-User-ID";
    private static final String HEADER_USERNAME = "X-Username";
    private static final String HEADER_USER_ROLES = "X-User-Roles";

    /**
     * Paths that do not require JWT authentication.
     */
    private static final Set<String> PUBLIC_PATH_PREFIXES = Set.of(
            "/actuator",
            "/health",
            "/info"
    );

    private final GatewayJwtProperties jwtProperties;

    public JwtValidationGatewayFilter(GatewayJwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!jwtProperties.isEnabled()) {
            return chain.filter(exchange);
        }

        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // Skip JWT validation for public paths
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        // No Authorization header — reject with 401
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            log.debug("No Bearer token found for request: {} {}", request.getMethod(), path);
            return unauthorizedResponse(exchange, "Missing or invalid Authorization header");
        }

        String token = authHeader.substring(BEARER_PREFIX.length());

        try {
            Claims claims = parseAndValidateToken(token);

            // Only accept access tokens
            String tokenType = claims.get(CLAIM_TOKEN_TYPE, String.class);
            if (!TOKEN_TYPE_ACCESS.equals(tokenType)) {
                log.debug("Rejected non-access token for request: {} {}", request.getMethod(), path);
                return unauthorizedResponse(exchange, "Invalid token type");
            }

            // Extract user information from claims
            String userId = claims.getSubject();
            String username = claims.get(CLAIM_USERNAME, String.class);
            if (username == null) {
                username = userId;
            }

            @SuppressWarnings("unchecked")
            List<String> roles = claims.get(CLAIM_ROLES, List.class);
            String rolesHeader = (roles != null) ? String.join(",", roles) : "";

            // Forward claims as headers to downstream services
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header(HEADER_USER_ID, userId)
                    .header(HEADER_USERNAME, username)
                    .header(HEADER_USER_ROLES, rolesHeader)
                    .build();

            log.debug("JWT validated for user '{}' with roles [{}] on {} {}",
                    username, rolesHeader, request.getMethod(), path);

            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch (ExpiredJwtException e) {
            log.warn("Expired JWT token for {} {}: {}", request.getMethod(), path, e.getMessage());
            return unauthorizedResponse(exchange, "Token has expired");
        } catch (JwtException e) {
            log.warn("Invalid JWT token for {} {}: {}", request.getMethod(), path, e.getMessage());
            return unauthorizedResponse(exchange, "Invalid token");
        } catch (IllegalArgumentException e) {
            log.warn("JWT processing error for {} {}: {}", request.getMethod(), path, e.getMessage());
            return unauthorizedResponse(exchange, "Token processing error");
        }
    }

    @Override
    public int getOrder() {
        // Run early in the filter chain, before routing
        return -100;
    }

    private boolean isPublicPath(String path) {
        for (String prefix : PUBLIC_PATH_PREFIXES) {
            if (path.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private Claims parseAndValidateToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(
                jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));

        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        // Verify issuer — reject tokens with missing or mismatched issuer
        String issuer = claims.getIssuer();
        String expectedIssuer = jwtProperties.getIssuer();
        if (expectedIssuer != null && !expectedIssuer.isBlank()) {
            if (issuer == null || !issuer.equals(expectedIssuer)) {
                throw new JwtException("Invalid or missing issuer: " + issuer);
            }
        }

        return claims;
    }

    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = String.format(
                "{\"error\":\"Unauthorized\",\"message\":\"%s\",\"status\":401}", message);
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
}
