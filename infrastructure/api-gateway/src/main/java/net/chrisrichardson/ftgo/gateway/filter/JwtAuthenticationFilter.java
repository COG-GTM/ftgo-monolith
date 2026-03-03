package net.chrisrichardson.ftgo.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Gateway filter that validates JWT tokens on incoming requests.
 *
 * <p>For authenticated requests, this filter:
 * <ol>
 *   <li>Extracts the Bearer token from the Authorization header</li>
 *   <li>Validates the token signature and expiration</li>
 *   <li>Extracts claims (subject, roles) from the token</li>
 *   <li>Forwards user identity as headers to downstream services:
 *       X-User-Id, X-User-Roles</li>
 * </ol>
 *
 * <p>Paths listed in {@code ftgo.gateway.jwt.excluded-paths} bypass JWT validation
 * (e.g., health checks, public endpoints).
 */
@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USER_ROLES = "X-User-Roles";

    @Value("${ftgo.gateway.jwt.secret}")
    private String jwtSecret;

    @Value("${ftgo.gateway.jwt.enabled:true}")
    private boolean jwtEnabled;

    @Value("#{'${ftgo.gateway.jwt.excluded-paths:/actuator/**,/fallback/**}'.split(',')}")
    private List<String> excludedPaths;

    public JwtAuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            if (!jwtEnabled) {
                return chain.filter(exchange);
            }

            String path = exchange.getRequest().getPath().value();

            // Skip JWT validation for excluded paths
            for (String excludedPath : excludedPaths) {
                String pattern = excludedPath.trim().replace("/**", "");
                if (path.startsWith(pattern)) {
                    return chain.filter(exchange);
                }
            }

            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            // If no Authorization header, reject the request
            if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
                log.warn("Missing or invalid Authorization header for path: {}", path);
                return onError(exchange, HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization header");
            }

            String token = authHeader.substring(BEARER_PREFIX.length());

            try {
                Claims claims = validateToken(token);

                // Forward user identity to downstream services
                ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                        .header(HEADER_USER_ID, claims.getSubject())
                        .header(HEADER_USER_ROLES, claims.get("roles", String.class) != null
                                ? claims.get("roles", String.class) : "")
                        .build();

                ServerWebExchange modifiedExchange = exchange.mutate()
                        .request(modifiedRequest)
                        .build();

                return chain.filter(modifiedExchange);

            } catch (ExpiredJwtException e) {
                log.warn("Expired JWT token for path: {}", path);
                return onError(exchange, HttpStatus.UNAUTHORIZED, "Token has expired");
            } catch (MalformedJwtException e) {
                log.warn("Malformed JWT token for path: {}", path);
                return onError(exchange, HttpStatus.UNAUTHORIZED, "Invalid token format");
            } catch (SignatureException e) {
                log.warn("Invalid JWT signature for path: {}", path);
                return onError(exchange, HttpStatus.UNAUTHORIZED, "Invalid token signature");
            } catch (Exception e) {
                log.error("JWT validation error for path: {}", path, e);
                return onError(exchange, HttpStatus.UNAUTHORIZED, "Token validation failed");
            }
        };
    }

    private Claims validateToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus status, String message) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().add("X-Gateway-Error", message);
        return exchange.getResponse().setComplete();
    }

    public static class Config {
        // Configuration properties for the filter (extensible)
    }
}
