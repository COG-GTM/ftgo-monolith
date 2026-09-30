package net.chrisrichardson.ftgo.orderservice.web.security;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final String BEARER_PREFIX = "Bearer ";

  private final JwtTokenVerifier tokenVerifier;
  private final AuthenticationEntryPoint authenticationEntryPoint;

  public JwtAuthenticationFilter(JwtTokenVerifier tokenVerifier, AuthenticationEntryPoint authenticationEntryPoint) {
    this.tokenVerifier = tokenVerifier;
    this.authenticationEntryPoint = authenticationEntryPoint;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
          throws ServletException, IOException {
    String header = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (header == null || !header.startsWith(BEARER_PREFIX)) {
      filterChain.doFilter(request, response);
      return;
    }

    FtgoPrincipal principal;
    try {
      principal = tokenVerifier.verify(header.substring(BEARER_PREFIX.length()).trim());
    } catch (BadCredentialsException e) {
      SecurityContextHolder.clearContext();
      authenticationEntryPoint.commence(request, response, e);
      return;
    }

    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(principal, null,
            Collections.singletonList(new SimpleGrantedAuthority(principal.getRole().authority())));
    SecurityContextHolder.getContext().setAuthentication(authentication);
    filterChain.doFilter(request, response);
  }
}
