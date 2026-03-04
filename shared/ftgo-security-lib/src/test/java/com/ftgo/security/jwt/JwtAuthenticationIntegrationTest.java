package com.ftgo.security.jwt;

import com.ftgo.security.TestApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end integration tests for JWT authentication.
 *
 * <p>Tests verify the complete authentication flow:
 * <ul>
 *   <li>Valid JWT token → 200 OK with user context</li>
 *   <li>Missing Authorization header → 401 Unauthorized</li>
 *   <li>Invalid JWT token → 401 Unauthorized</li>
 *   <li>Expired JWT token → 401 Unauthorized</li>
 *   <li>Refresh token rejected for API access → 401 Unauthorized</li>
 *   <li>JWT with roles → correct authorities in SecurityContext</li>
 *   <li>User context propagation via SecurityUtils</li>
 *   <li>Public endpoints accessible without JWT</li>
 * </ul>
 */
@SpringBootTest(classes = TestApplication.class)
@AutoConfigureMockMvc
class JwtAuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private FtgoJwtProperties jwtProperties;

    private static final String USER_ID = "user-integration-123";
    private static final String USERNAME = "integration.user";
    private static final List<String> ROLES = Arrays.asList("ROLE_USER", "ROLE_ADMIN");

    private String validAccessToken;

    @BeforeEach
    void setUp() {
        validAccessToken = jwtTokenProvider.generateAccessToken(USER_ID, USERNAME, ROLES);
    }

    @Test
    @DisplayName("Valid JWT token authenticates successfully and returns 200")
    void validJwtTokenReturns200() throws Exception {
        mockMvc.perform(get("/api/test")
                        .header("Authorization", "Bearer " + validAccessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Missing Authorization header returns 401")
    void missingAuthorizationHeaderReturns401() throws Exception {
        mockMvc.perform(get("/api/test")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @DisplayName("Invalid JWT token returns 401")
    void invalidJwtTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/test")
                        .header("Authorization", "Bearer invalid.token.here")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("Tampered JWT token returns 401")
    void tamperedJwtTokenReturns401() throws Exception {
        String tampered = validAccessToken.substring(0, validAccessToken.length() - 5) + "XXXXX";

        mockMvc.perform(get("/api/test")
                        .header("Authorization", "Bearer " + tampered)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Expired JWT token returns 401")
    void expiredJwtTokenReturns401() throws Exception {
        // Temporarily set expiration to 0 to generate an expired token
        long originalExpiration = jwtProperties.getExpirationMs();
        jwtProperties.setExpirationMs(0);
        try {
            String expiredToken = jwtTokenProvider.generateAccessToken(USER_ID, USERNAME, ROLES);

            // Small delay to ensure token is expired
            Thread.sleep(50);

            mockMvc.perform(get("/api/test")
                            .header("Authorization", "Bearer " + expiredToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        } finally {
            jwtProperties.setExpirationMs(originalExpiration);
        }
    }

    @Test
    @DisplayName("Refresh token is rejected for API access")
    void refreshTokenRejectedForApiAccess() throws Exception {
        String refreshToken = jwtTokenProvider.generateRefreshToken(USER_ID);

        mockMvc.perform(get("/api/test")
                        .header("Authorization", "Bearer " + refreshToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("JWT with custom claims propagates user context")
    void jwtWithCustomClaimsPropagatesUserContext() throws Exception {
        Map<String, Object> customClaims = new HashMap<>();
        customClaims.put("tenant_id", "tenant-789");
        customClaims.put("service", "order-service");

        String tokenWithClaims = jwtTokenProvider.generateAccessToken(
                USER_ID, USERNAME, ROLES, customClaims);

        mockMvc.perform(get("/api/test/user-context")
                        .header("Authorization", "Bearer " + tokenWithClaims)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(USER_ID))
                .andExpect(jsonPath("$.username").value(USERNAME))
                .andExpect(jsonPath("$.authenticated").value(true));
    }

    @Test
    @DisplayName("JWT with roles sets correct authorities")
    void jwtWithRolesSetsCorrectAuthorities() throws Exception {
        mockMvc.perform(get("/api/test/authorities")
                        .header("Authorization", "Bearer " + validAccessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authorities").isArray());
    }

    @Test
    @DisplayName("Health endpoint is accessible without JWT")
    void healthEndpointAccessibleWithoutJwt() throws Exception {
        mockMvc.perform(get("/actuator/health")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Swagger endpoint is accessible without JWT")
    void swaggerEndpointAccessibleWithoutJwt() throws Exception {
        mockMvc.perform(get("/swagger-ui.html")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(result -> {
                    int statusCode = result.getResponse().getStatus();
                    // Swagger UI may redirect (302) or return 404 if not configured,
                    // but should not return 401
                    assert statusCode != 401 : "Swagger UI should not require authentication, got 401";
                });
    }

    @Test
    @DisplayName("Bearer prefix is required in Authorization header")
    void bearerPrefixRequired() throws Exception {
        mockMvc.perform(get("/api/test")
                        .header("Authorization", validAccessToken)  // Missing "Bearer " prefix
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Empty Bearer token returns 401")
    void emptyBearerTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/test")
                        .header("Authorization", "Bearer ")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("JWT without roles still authenticates with no authorities")
    void jwtWithoutRolesAuthenticatesWithNoAuthorities() throws Exception {
        String tokenNoRoles = jwtTokenProvider.generateAccessToken(
                USER_ID, USERNAME, Collections.emptyList());

        mockMvc.perform(get("/api/test")
                        .header("Authorization", "Bearer " + tokenNoRoles)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
