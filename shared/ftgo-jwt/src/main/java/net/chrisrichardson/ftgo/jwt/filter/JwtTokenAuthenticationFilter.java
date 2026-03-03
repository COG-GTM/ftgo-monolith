package net.chrisrichardson.ftgo.jwt.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import net.chrisrichardson.ftgo.jwt.FtgoJwtProperties;
import net.chrisrichardson.ftgo.jwt.model.FtgoUserContext;
import net.chrisrichardson.ftgo.jwt.service.JwtTokenService;
import net.chrisrichardson.ftgo.jwt.util.JwtUserContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Servlet filter that validates JWT tokens and populates the Spring Security context.
 *
 * <p>This filter intercepts incoming HTTP requests and performs the following:</p>
 * <ol>
 *   <li>Checks if the request path is excluded from JWT validation</li>
 *   <li>Extracts the Bearer token from the Authorization header</li>
 *   <li>Validates the token signature, expiration, and claims</li>
 *   <li>Populates the {@link SecurityContextHolder} with the authenticated user</li>
 *   <li>Sets the {@link FtgoUserContext} in the {@link JwtUserContextHolder}</li>
 * </ol>
 *
 * <p>If the token is missing, the filter allows the request to continue
 * (subsequent security filters will enforce authentication). If the token
 * is present but invalid, the filter clears the security context.</p>
 *
 * <p>This filter extends {@link OncePerRequestFilter} to ensure it runs
 * exactly once per request, even in forwarded/included request scenarios.</p>
 *
 * @see JwtTokenService
 * @see JwtUserContextHolder
 */
public class JwtTokenAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenAuthenticationFilter.class);

    private final JwtTokenService jwtTokenService;
    private final FtgoJwtProperties jwtProperties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public JwtTokenAuthenticationFilter(JwtTokenService jwtTokenService,
                                        FtgoJwtProperties jwtProperties) {
        this.jwtTokenService = jwtTokenService;
        this.jwtProperties = jwtProperties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            if (!jwtProperties.isEnabled()) {
                filterChain.doFilter(request, response);
                return;
            }

            String requestPath = request.getRequestURI();

            // Skip excluded paths
            if (isExcludedPath(requestPath)) {
                filterChain.doFilter(request, response);
                return;
            }

            String token = extractToken(request);

            if (token != null) {
                try {
                    Claims claims = jwtTokenService.validateAndExtractClaims(token);
                    FtgoUserContext userContext = jwtTokenService.extractUserContext(claims);

                    // Build Spring Security authentication
                    List<GrantedAuthority> authorities = buildAuthorities(userContext);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userContext.getUserId(), null, authorities);
                    authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));

                    // Set the security context
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    // Set the user context for service-layer access
                    JwtUserContextHolder.setCurrentUser(userContext);

                    log.debug("Authenticated user '{}' with roles: {}",
                            userContext.getUserId(), userContext.getRoles());

                } catch (ExpiredJwtException e) {
                    log.warn("Expired JWT token for request: {} {}", request.getMethod(), requestPath);
                    SecurityContextHolder.clearContext();
                    JwtUserContextHolder.clear();
                } catch (MalformedJwtException e) {
                    log.warn("Malformed JWT token for request: {} {}", request.getMethod(), requestPath);
                    SecurityContextHolder.clearContext();
                    JwtUserContextHolder.clear();
                } catch (SignatureException e) {
                    log.warn("Invalid JWT signature for request: {} {}", request.getMethod(), requestPath);
                    SecurityContextHolder.clearContext();
                    JwtUserContextHolder.clear();
                } catch (JwtException e) {
                    log.warn("JWT validation failed for request: {} {} - {}",
                            request.getMethod(), requestPath, e.getMessage());
                    SecurityContextHolder.clearContext();
                    JwtUserContextHolder.clear();
                }
            }

            filterChain.doFilter(request, response);
        } finally {
            // Always clean up the thread-local user context
            JwtUserContextHolder.clear();
        }
    }

    /**
     * Extracts the JWT token from the Authorization header.
     *
     * @param request the HTTP request
     * @return the token string, or null if no valid Bearer token is found
     */
    private String extractToken(HttpServletRequest request) {
        String headerValue = request.getHeader(jwtProperties.getHeaderName());
        if (headerValue != null && headerValue.startsWith(jwtProperties.getTokenPrefix())) {
            return headerValue.substring(jwtProperties.getTokenPrefix().length()).trim();
        }
        return null;
    }

    /**
     * Checks if the request path should be excluded from JWT validation.
     *
     * @param requestPath the request URI
     * @return true if the path is excluded
     */
    private boolean isExcludedPath(String requestPath) {
        for (String excludedPath : jwtProperties.getExcludedPaths()) {
            if (pathMatcher.match(excludedPath, requestPath)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Builds Spring Security authorities from the user context.
     * Roles are prefixed with "ROLE_" per Spring Security convention.
     * Permissions are added as-is.
     *
     * @param userContext the user context
     * @return list of granted authorities
     */
    private List<GrantedAuthority> buildAuthorities(FtgoUserContext userContext) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        // Add roles with ROLE_ prefix
        for (String role : userContext.getRoles()) {
            String authority = role.startsWith("ROLE_") ? role : "ROLE_" + role;
            authorities.add(new SimpleGrantedAuthority(authority));
        }

        // Add permissions as-is
        for (String permission : userContext.getPermissions()) {
            authorities.add(new SimpleGrantedAuthority(permission));
        }

        return authorities;
    }
}
