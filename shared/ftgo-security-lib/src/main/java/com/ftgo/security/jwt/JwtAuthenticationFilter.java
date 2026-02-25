package com.ftgo.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Servlet filter that extracts a JWT from the {@code Authorization} header,
 * validates it, and populates the {@link SecurityContextHolder} with the
 * resulting {@link Authentication}.
 * <p>
 * The filter expects tokens in the standard Bearer scheme:
 * <pre>
 * Authorization: Bearer &lt;token&gt;
 * </pre>
 * </p>
 * <p>
 * Only <em>access</em> tokens are accepted for API authentication.
 * Refresh tokens presented in the Authorization header are rejected
 * (they should only be sent to the dedicated refresh endpoint).
 * </p>
 *
 * @see JwtTokenProvider
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider tokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);

        if (token != null && tokenProvider.validateToken(token)) {
            if (tokenProvider.isAccessToken(token)) {
                Authentication authentication = tokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("Set authentication for user '{}' from JWT",
                        authentication.getName());
            } else {
                log.debug("Rejected non-access token on API request to {}",
                        request.getRequestURI());
            }
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            // Clear context after request to avoid leaking across threads in
            // async / pooled-thread scenarios.
            SecurityContextHolder.clearContext();
        }
    }

    /**
     * Extracts the raw JWT string from the Authorization header.
     *
     * @param request the HTTP request
     * @return the token string, or {@code null} if the header is absent or
     *         does not start with "Bearer "
     */
    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader(AUTHORIZATION_HEADER);
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
