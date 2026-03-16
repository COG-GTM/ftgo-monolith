package net.chrisrichardson.ftgo.security.jwt;

import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Servlet filter that intercepts incoming HTTP requests, extracts a JWT from the
 * {@code Authorization} header, validates it, and populates the Spring Security
 * context with a {@link JwtAuthenticationToken}.
 *
 * <p>This filter extends {@link OncePerRequestFilter} to guarantee single execution
 * per request even when the request is forwarded internally.
 *
 * <p>Processing logic:
 * <ol>
 *   <li>Extract the {@code Authorization} header</li>
 *   <li>Strip the {@code Bearer } prefix</li>
 *   <li>Validate the token and parse claims</li>
 *   <li>Reject refresh tokens (only access tokens are accepted for API calls)</li>
 *   <li>Build a {@link JwtAuthenticationToken} with authorities derived from roles and permissions</li>
 *   <li>Set the authentication in the {@link SecurityContextHolder}</li>
 * </ol>
 *
 * <p>If the token is missing, invalid, or expired, the filter does nothing and
 * lets the request continue unauthenticated — Spring Security's
 * {@link org.springframework.security.web.access.ExceptionTranslationFilter}
 * will handle the 401 response.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenValidator tokenValidator;
    private final JwtProperties properties;

    public JwtAuthenticationFilter(JwtTokenValidator tokenValidator, JwtProperties properties) {
        this.tokenValidator = tokenValidator;
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = extractToken(request);

        if (token != null) {
            Claims claims = tokenValidator.validateAndExtractClaims(token);

            if (claims != null) {
                if (tokenValidator.isRefreshToken(claims)) {
                    log.warn("Refresh token used for API access — rejected (URI: {})", request.getRequestURI());
                } else {
                    String userId = JwtClaimsExtractor.getUserId(claims);
                    List<String> roles = JwtClaimsExtractor.getRoles(claims);
                    List<String> permissions = JwtClaimsExtractor.getPermissions(claims);

                    List<GrantedAuthority> authorities = buildAuthorities(roles, permissions);

                    JwtAuthenticationToken authentication =
                            new JwtAuthenticationToken(userId, roles, permissions, authorities);

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("JWT authentication set for user: {}", userId);
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader(properties.getHeader());
        if (header != null && header.startsWith(properties.getTokenPrefix())) {
            return header.substring(properties.getTokenPrefix().length());
        }
        return null;
    }

    private List<GrantedAuthority> buildAuthorities(List<String> roles, List<String> permissions) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        for (String role : roles) {
            authorities.add(new SimpleGrantedAuthority(role));
        }
        for (String permission : permissions) {
            authorities.add(new SimpleGrantedAuthority(permission));
        }
        return authorities;
    }
}
