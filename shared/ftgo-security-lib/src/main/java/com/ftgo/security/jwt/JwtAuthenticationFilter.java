package com.ftgo.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT authentication filter that intercepts incoming HTTP requests,
 * extracts the Bearer token from the Authorization header, validates it,
 * and sets the authentication in the {@code SecurityContextHolder}.
 *
 * <p>This filter extends {@link OncePerRequestFilter} to guarantee single
 * execution per request. It is added to the Spring Security filter chain
 * before the standard authentication filters.
 *
 * <p>Request flow:
 * <ol>
 *   <li>Extract the {@code Authorization: Bearer <token>} header</li>
 *   <li>Validate the JWT token (signature, expiration, issuer)</li>
 *   <li>Extract user details from the token claims</li>
 *   <li>Create a {@link JwtAuthenticationToken} and set it in the security context</li>
 *   <li>Continue the filter chain</li>
 * </ol>
 *
 * <p>If the token is missing, invalid, or expired, the filter simply continues
 * the chain without setting authentication — downstream security filters will
 * reject the request with a 401 response.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = extractTokenFromRequest(request);

            if (token != null && jwtTokenProvider.validateToken(token)) {
                // Only accept access tokens (not refresh tokens) for API authentication
                if (!jwtTokenProvider.isAccessToken(token)) {
                    log.debug("Rejected non-access token for request: {} {}",
                            request.getMethod(), request.getRequestURI());
                } else {
                    FtgoUserDetails userDetails = jwtTokenProvider.extractUserDetails(token);

                    List<SimpleGrantedAuthority> authorities = userDetails.getRoles().stream()
                            .map(role -> new SimpleGrantedAuthority(
                                    role.startsWith("ROLE_") ? role : "ROLE_" + role))
                            .collect(Collectors.toList());

                    JwtAuthenticationToken authentication =
                            new JwtAuthenticationToken(userDetails, token, authorities);

                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    log.debug("Authenticated user '{}' with roles {} for {} {}",
                            userDetails.getUsername(), userDetails.getRoles(),
                            request.getMethod(), request.getRequestURI());
                }
            }
        } catch (Exception e) {
            log.warn("Failed to process JWT authentication for {} {}: {}",
                    request.getMethod(), request.getRequestURI(), e.getMessage());
            // Clear context on any error to ensure no partial authentication
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extracts the JWT token from the Authorization header.
     *
     * @param request the HTTP request
     * @return the JWT token string, or null if not present
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (bearerToken != null && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
