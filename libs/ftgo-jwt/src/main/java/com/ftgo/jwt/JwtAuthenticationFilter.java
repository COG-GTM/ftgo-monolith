package com.ftgo.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenProvider tokenProvider;
    private final JwtProperties properties;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider, JwtProperties properties) {
        this.tokenProvider = tokenProvider;
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = extractToken(request);

        if (token != null && tokenProvider.validateToken(token)) {
            String subject = tokenProvider.getSubject(token);
            List<String> roles = tokenProvider.getRoles(token);

            List<SimpleGrantedAuthority> authorities = roles.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(subject, null, authorities);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug("Set authentication for subject: {}", subject);
        }

        filterChain.doFilter(request, response);
    }

    String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(properties.getHeader());
        if (bearerToken != null && bearerToken.startsWith(properties.getPrefix())) {
            return bearerToken.substring(properties.getPrefix().length());
        }
        return null;
    }
}
