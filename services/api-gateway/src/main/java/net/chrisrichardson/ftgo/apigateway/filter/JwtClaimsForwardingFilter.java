package net.chrisrichardson.ftgo.apigateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Global filter that extracts JWT claims from the authenticated security context
 * and forwards them as headers to downstream microservices.
 *
 * Headers forwarded:
 * <ul>
 *   <li>{@code X-User-Id} - The subject (sub) claim from the JWT</li>
 *   <li>{@code X-User-Email} - The email claim from the JWT (if present)</li>
 *   <li>{@code X-User-Roles} - Comma-separated roles/authorities from the JWT</li>
 * </ul>
 */
@Component
public class JwtClaimsForwardingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(JwtClaimsForwardingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
                .filter(ctx -> ctx.getAuthentication() instanceof JwtAuthenticationToken)
                .map(ctx -> (JwtAuthenticationToken) ctx.getAuthentication())
                .map(jwtAuth -> {
                    Jwt jwt = jwtAuth.getToken();
                    ServerHttpRequest.Builder requestBuilder = exchange.getRequest().mutate();

                    // Forward subject as user ID
                    String subject = jwt.getSubject();
                    if (subject != null) {
                        requestBuilder.header("X-User-Id", subject);
                    }

                    // Forward email claim if present
                    String email = jwt.getClaimAsString("email");
                    if (email != null) {
                        requestBuilder.header("X-User-Email", email);
                    }

                    // Forward roles/authorities
                    String roles = String.join(",",
                            jwtAuth.getAuthorities().stream()
                                    .map(Object::toString)
                                    .toList());
                    if (!roles.isEmpty()) {
                        requestBuilder.header("X-User-Roles", roles);
                    }

                    log.debug("Forwarding JWT claims for user: {}", subject);
                    return exchange.mutate().request(requestBuilder.build()).build();
                })
                .defaultIfEmpty(exchange)
                .flatMap(chain::filter);
    }

    @Override
    public int getOrder() {
        // Run after security filter but before routing
        return -1;
    }
}
