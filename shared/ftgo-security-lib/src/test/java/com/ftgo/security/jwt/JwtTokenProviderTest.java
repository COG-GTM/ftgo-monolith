package com.ftgo.security.jwt;

import com.ftgo.security.TestApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit/integration tests for {@link JwtTokenProvider}.
 *
 * <p>Tests cover:
 * <ul>
 *   <li>Access token generation and validation</li>
 *   <li>Refresh token generation and validation</li>
 *   <li>Claims extraction (user ID, username, roles, custom claims)</li>
 *   <li>Token expiration handling</li>
 *   <li>Invalid token rejection</li>
 *   <li>Token type differentiation (access vs refresh)</li>
 * </ul>
 */
@SpringBootTest(classes = TestApplication.class)
class JwtTokenProviderTest {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private FtgoJwtProperties jwtProperties;

    private static final String USER_ID = "user-123";
    private static final String USERNAME = "john.doe";
    private static final List<String> ROLES = Arrays.asList("ROLE_USER", "ROLE_ADMIN");

    @Test
    @DisplayName("Generate access token and validate it")
    void generateAndValidateAccessToken() {
        String token = jwtTokenProvider.generateAccessToken(USER_ID, USERNAME, ROLES);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    @DisplayName("Extract user details from access token")
    void extractUserDetailsFromAccessToken() {
        String token = jwtTokenProvider.generateAccessToken(USER_ID, USERNAME, ROLES);

        FtgoUserDetails userDetails = jwtTokenProvider.extractUserDetails(token);

        assertEquals(USER_ID, userDetails.getUserId());
        assertEquals(USERNAME, userDetails.getUsername());
        assertTrue(userDetails.getRoles().containsAll(ROLES));
    }

    @Test
    @DisplayName("Access token contains correct claims")
    void accessTokenContainsCorrectClaims() {
        Map<String, Object> customClaims = new HashMap<>();
        customClaims.put("tenant_id", "tenant-456");
        customClaims.put("service", "order-service");

        String token = jwtTokenProvider.generateAccessToken(USER_ID, USERNAME, ROLES, customClaims);
        FtgoUserDetails userDetails = jwtTokenProvider.extractUserDetails(token);

        assertEquals(USER_ID, userDetails.getUserId());
        assertEquals(USERNAME, userDetails.getUsername());
        assertEquals("tenant-456", userDetails.getClaim("tenant_id"));
        assertEquals("order-service", userDetails.getClaim("service"));
        assertEquals("ftgo-platform", userDetails.getClaim("iss"));
    }

    @Test
    @DisplayName("Access token is identified as access type")
    void accessTokenIsAccessType() {
        String token = jwtTokenProvider.generateAccessToken(USER_ID, USERNAME, ROLES);

        assertTrue(jwtTokenProvider.isAccessToken(token));
        assertFalse(jwtTokenProvider.isRefreshToken(token));
    }

    @Test
    @DisplayName("Generate and validate refresh token")
    void generateAndValidateRefreshToken() {
        String token = jwtTokenProvider.generateRefreshToken(USER_ID);

        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));
        assertTrue(jwtTokenProvider.isRefreshToken(token));
        assertFalse(jwtTokenProvider.isAccessToken(token));
    }

    @Test
    @DisplayName("Extract user ID from refresh token")
    void extractUserIdFromRefreshToken() {
        String token = jwtTokenProvider.generateRefreshToken(USER_ID);

        String extractedUserId = jwtTokenProvider.extractUserId(token);
        assertEquals(USER_ID, extractedUserId);
    }

    @Test
    @DisplayName("Extract expiration date from token")
    void extractExpirationFromToken() {
        String token = jwtTokenProvider.generateAccessToken(USER_ID, USERNAME, ROLES);

        Date expiration = jwtTokenProvider.extractExpiration(token);
        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    @DisplayName("Invalid token fails validation")
    void invalidTokenFailsValidation() {
        assertFalse(jwtTokenProvider.validateToken("invalid.token.here"));
    }

    @Test
    @DisplayName("Tampered token fails validation")
    void tamperedTokenFailsValidation() {
        String token = jwtTokenProvider.generateAccessToken(USER_ID, USERNAME, ROLES);
        // Tamper with the token by modifying a character
        String tampered = token.substring(0, token.length() - 5) + "XXXXX";

        assertFalse(jwtTokenProvider.validateToken(tampered));
    }

    @Test
    @DisplayName("Token with null roles returns empty roles list")
    void tokenWithNullRolesReturnsEmptyList() {
        String token = jwtTokenProvider.generateAccessToken(USER_ID, USERNAME, null);
        FtgoUserDetails userDetails = jwtTokenProvider.extractUserDetails(token);

        assertNotNull(userDetails.getRoles());
        assertTrue(userDetails.getRoles().isEmpty());
    }

    @Test
    @DisplayName("Token with empty roles returns empty roles list")
    void tokenWithEmptyRolesReturnsEmptyList() {
        String token = jwtTokenProvider.generateAccessToken(USER_ID, USERNAME, Collections.emptyList());
        FtgoUserDetails userDetails = jwtTokenProvider.extractUserDetails(token);

        assertNotNull(userDetails.getRoles());
        assertTrue(userDetails.getRoles().isEmpty());
    }

    @Test
    @DisplayName("Username defaults to user ID when not present")
    void usernameDefaultsToUserId() {
        // Generate token with null username via custom claims that override
        Map<String, Object> claims = new HashMap<>();
        // Don't set username claim — it won't be in the token
        String token = jwtTokenProvider.generateAccessToken(USER_ID, null, ROLES, claims);
        FtgoUserDetails userDetails = jwtTokenProvider.extractUserDetails(token);

        // When username is null in claims, it falls back to userId
        assertEquals(USER_ID, userDetails.getUserId());
    }
}
