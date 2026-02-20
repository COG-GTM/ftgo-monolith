package com.ftgo.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    private static final String SECRET = "this-is-a-very-secure-secret-key-for-testing-jwt-operations-256bit";
    private static final String ISSUER = "ftgo-test";

    private JwtTokenProvider tokenProvider;
    private JwtAuthenticationFilter filter;
    private JwtProperties properties;

    @Mock
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        properties = new JwtProperties();
        properties.setSecret(SECRET);
        properties.setIssuer(ISSUER);
        properties.setExpiration(3600000);
        tokenProvider = new JwtTokenProvider(properties);
        filter = new JwtAuthenticationFilter(tokenProvider, properties);
    }

    @Test
    void shouldSetAuthenticationForValidToken() throws ServletException, IOException {
        Map<String, Object> claims = Map.of("roles", List.of("ROLE_USER"));
        String token = tokenProvider.generateToken("user@example.com", claims);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getName()).isEqualTo("user@example.com");
        assertThat(authentication.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_USER");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldSetAuthenticationWithMultipleRoles() throws ServletException, IOException {
        Map<String, Object> claims = Map.of("roles", List.of("ROLE_USER", "ROLE_ADMIN"));
        String token = tokenProvider.generateToken("admin@example.com", claims);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getAuthorities())
                .extracting("authority")
                .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotSetAuthenticationForMissingHeader() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotSetAuthenticationForInvalidToken() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalid-token-here");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotSetAuthenticationForMissingPrefix() throws ServletException, IOException {
        String token = tokenProvider.generateToken("user@example.com");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldAlwaysContinueFilterChain() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer bad-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldExtractTokenFromHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer my-jwt-token");

        String token = filter.extractToken(request);

        assertThat(token).isEqualTo("my-jwt-token");
    }

    @Test
    void shouldReturnNullWhenNoAuthorizationHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        String token = filter.extractToken(request);

        assertThat(token).isNull();
    }

    @Test
    void shouldReturnNullWhenWrongPrefix() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic credentials");

        String token = filter.extractToken(request);

        assertThat(token).isNull();
    }

    @Test
    void shouldSupportCustomHeaderAndPrefix() {
        JwtProperties customProps = new JwtProperties();
        customProps.setSecret(SECRET);
        customProps.setIssuer(ISSUER);
        customProps.setExpiration(3600000);
        customProps.setHeader("X-Auth-Token");
        customProps.setPrefix("Token ");

        JwtAuthenticationFilter customFilter = new JwtAuthenticationFilter(tokenProvider, customProps);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Auth-Token", "Token my-jwt-token");

        String token = customFilter.extractToken(request);

        assertThat(token).isEqualTo("my-jwt-token");
    }
}
