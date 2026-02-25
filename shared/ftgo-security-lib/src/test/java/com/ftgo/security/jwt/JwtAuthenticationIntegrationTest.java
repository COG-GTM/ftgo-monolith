package com.ftgo.security.jwt;

import com.ftgo.security.config.FtgoBaseSecurityConfiguration;
import com.ftgo.security.config.FtgoCorsConfiguration;
import com.ftgo.security.exception.FtgoAccessDeniedHandler;
import com.ftgo.security.exception.FtgoAuthenticationEntryPoint;
import com.ftgo.security.filter.CorrelationIdFilter;
import com.ftgo.security.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-end integration tests for JWT authentication.
 * <p>
 * Tests the full filter chain: JWT filter → security configuration →
 * controller, verifying that:
 * <ul>
 *   <li>Valid access tokens grant access to protected endpoints</li>
 *   <li>Invalid/expired tokens return 401</li>
 *   <li>Refresh tokens are rejected for API access</li>
 *   <li>User context is available via SecurityUtils</li>
 * </ul>
 */
@SpringBootTest(
    classes = JwtAuthenticationIntegrationTest.JwtTestApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.MOCK,
    properties = {
        "ftgo.security.jwt.enabled=true",
        "ftgo.security.jwt.secret=integration-test-secret-that-is-at-least-32-bytes-long!!",
        "ftgo.security.jwt.issuer=ftgo-test",
        "ftgo.security.jwt.access-token-expiration=PT15M",
        "ftgo.security.jwt.refresh-token-expiration=P7D"
    }
)
@AutoConfigureMockMvc
class JwtAuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private JwtTokenService tokenService;

    private String validAccessToken;
    private String validRefreshToken;

    /**
     * Dedicated test configuration that only registers the JWT test controller
     * to avoid handler mapping conflicts with other test classes.
     */
    @Configuration
    @EnableAutoConfiguration
    @ComponentScan(
        basePackageClasses = JwtAuthenticationIntegrationTest.class,
        includeFilters = @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = JwtTestController.class
        ),
        useDefaultFilters = false
    )
    @Import({
        FtgoBaseSecurityConfiguration.class,
        FtgoCorsConfiguration.class,
        FtgoAuthenticationEntryPoint.class,
        FtgoAccessDeniedHandler.class,
        CorrelationIdFilter.class,
        JwtAutoConfiguration.class
    })
    static class JwtTestApplication {
    }

    @RestController
    static class JwtTestController {

        @GetMapping("/api/jwt-test")
        public String test() {
            return "OK";
        }

        @GetMapping(value = "/api/jwt-user-context", produces = MediaType.APPLICATION_JSON_VALUE)
        public Map<String, Object> userContext() {
            Map<String, Object> result = new HashMap<>();
            result.put("username", SecurityUtils.getCurrentUsername().orElse("anonymous"));
            result.put("userId", SecurityUtils.getCurrentUserId().orElse(-1L));
            result.put("roles", SecurityUtils.getCurrentRoles());
            result.put("permissions", SecurityUtils.getCurrentPermissions());
            result.put("authenticated", SecurityUtils.isAuthenticated());
            return result;
        }
    }

    @BeforeEach
    void setUp() {
        JwtTokenResponse tokens = tokenService.issueTokens(
                42L, "john.doe",
                List.of("ROLE_USER", "ROLE_ADMIN"),
                List.of("order:create", "order:read"));
        validAccessToken = tokens.getAccessToken();
        validRefreshToken = tokens.getRefreshToken();
    }

    @Nested
    @DisplayName("Unauthenticated Requests")
    class UnauthenticatedRequests {

        @Test
        @DisplayName("should return 401 for requests without Authorization header")
        void shouldReturn401WithoutAuthHeader() throws Exception {
            mockMvc.perform(get("/api/jwt-test"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
        }

        @Test
        @DisplayName("should return 401 for requests with empty Bearer token")
        void shouldReturn401WithEmptyBearer() throws Exception {
            mockMvc.perform(get("/api/jwt-test")
                    .header("Authorization", "Bearer "))
                .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Valid JWT Authentication")
    class ValidJwtAuthentication {

        @Test
        @DisplayName("should allow access with valid access token")
        void shouldAllowAccessWithValidToken() throws Exception {
            mockMvc.perform(get("/api/jwt-test")
                    .header("Authorization", "Bearer " + validAccessToken))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));
        }

        @Test
        @DisplayName("should populate user context from JWT claims")
        void shouldPopulateUserContext() throws Exception {
            mockMvc.perform(get("/api/jwt-user-context")
                    .header("Authorization", "Bearer " + validAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("john.doe"))
                .andExpect(jsonPath("$.userId").value(42))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"))
                .andExpect(jsonPath("$.roles[1]").value("ROLE_ADMIN"))
                .andExpect(jsonPath("$.permissions[0]").value("order:create"))
                .andExpect(jsonPath("$.permissions[1]").value("order:read"))
                .andExpect(jsonPath("$.authenticated").value(true));
        }
    }

    @Nested
    @DisplayName("Invalid JWT Authentication")
    class InvalidJwtAuthentication {

        @Test
        @DisplayName("should return 401 for malformed token")
        void shouldReturn401ForMalformedToken() throws Exception {
            mockMvc.perform(get("/api/jwt-test")
                    .header("Authorization", "Bearer not.a.valid.jwt"))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("should return 401 for expired token")
        void shouldReturn401ForExpiredToken() throws Exception {
            // Create a provider with instant expiry
            JwtProperties shortProps = new JwtProperties();
            shortProps.setSecret("integration-test-secret-that-is-at-least-32-bytes-long!!");
            shortProps.setIssuer("ftgo-test");
            shortProps.setAccessTokenExpiration(Duration.ofMillis(1));
            JwtTokenProvider shortProvider = new JwtTokenProvider(shortProps);

            String expiredToken = shortProvider.generateAccessToken(
                    1L, "user", List.of(), List.of());
            Thread.sleep(50);

            mockMvc.perform(get("/api/jwt-test")
                    .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("should return 401 for token signed with wrong key")
        void shouldReturn401ForWrongSignature() throws Exception {
            JwtProperties wrongProps = new JwtProperties();
            wrongProps.setSecret("wrong-secret-key-that-is-at-least-32-bytes-long-too!!!");
            wrongProps.setIssuer("ftgo-test");
            wrongProps.setAccessTokenExpiration(Duration.ofMinutes(15));
            JwtTokenProvider wrongProvider = new JwtTokenProvider(wrongProps);

            String wrongToken = wrongProvider.generateAccessToken(
                    1L, "user", List.of(), List.of());

            mockMvc.perform(get("/api/jwt-test")
                    .header("Authorization", "Bearer " + wrongToken))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("should reject refresh token used for API access")
        void shouldRejectRefreshTokenForApiAccess() throws Exception {
            mockMvc.perform(get("/api/jwt-test")
                    .header("Authorization", "Bearer " + validRefreshToken))
                .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Public Endpoints")
    class PublicEndpoints {

        @Test
        @DisplayName("should allow access to health endpoint without token")
        void shouldAllowHealthWithoutToken() throws Exception {
            mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
        }
    }
}
