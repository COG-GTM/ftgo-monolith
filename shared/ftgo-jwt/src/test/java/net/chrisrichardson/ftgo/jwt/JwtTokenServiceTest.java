package net.chrisrichardson.ftgo.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import net.chrisrichardson.ftgo.jwt.model.FtgoUserContext;
import net.chrisrichardson.ftgo.jwt.model.TokenPair;
import net.chrisrichardson.ftgo.jwt.service.JwtTokenService;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link JwtTokenService}.
 *
 * <p>Tests cover token generation, validation, claims extraction,
 * refresh mechanism, and error handling for invalid/expired tokens.</p>
 */
public class JwtTokenServiceTest {

    // 256-bit (32-byte) secret for HS256 signing
    private static final String TEST_SECRET = "ftgo-test-secret-key-that-is-at-least-32-bytes-long!!";
    private static final String TEST_ISSUER = "https://keycloak.ftgo.com/realms/ftgo";

    private JwtTokenService jwtTokenService;
    private FtgoJwtProperties jwtProperties;

    @Before
    public void setUp() {
        jwtProperties = new FtgoJwtProperties();
        jwtProperties.setSecret(TEST_SECRET);
        jwtProperties.setIssuer(TEST_ISSUER);
        jwtProperties.setExpirationSeconds(3600);
        jwtProperties.setRefreshExpirationSeconds(86400);
        jwtTokenService = new JwtTokenService(jwtProperties);
    }

    // -------------------------------------------------------------------------
    // Token Generation Tests
    // -------------------------------------------------------------------------

    @Test
    public void shouldGenerateAccessToken() {
        FtgoUserContext userContext = createTestUserContext();

        String token = jwtTokenService.generateAccessToken(userContext);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        // JWT tokens have 3 parts separated by dots
        assertEquals(3, token.split("\\.").length);
    }

    @Test
    public void shouldGenerateTokenPair() {
        FtgoUserContext userContext = createTestUserContext();

        TokenPair tokenPair = jwtTokenService.generateTokenPair(userContext);

        assertNotNull(tokenPair);
        assertNotNull(tokenPair.getAccessToken());
        assertNotNull(tokenPair.getRefreshToken());
        assertNotEquals(tokenPair.getAccessToken(), tokenPair.getRefreshToken());
        assertEquals("Bearer", tokenPair.getTokenType());
        assertNotNull(tokenPair.getExpiresAt());
        assertNotNull(tokenPair.getRefreshExpiresAt());
        assertFalse(tokenPair.isAccessTokenExpired());
        assertFalse(tokenPair.isRefreshTokenExpired());
    }

    @Test
    public void shouldGenerateTokenWithCorrectClaims() {
        FtgoUserContext userContext = FtgoUserContext.builder()
                .userId("user-123")
                .username("john.doe")
                .roles(Arrays.asList("ADMIN", "USER"))
                .permissions(Arrays.asList("order:read", "order:write"))
                .build();

        String token = jwtTokenService.generateAccessToken(userContext);
        Claims claims = jwtTokenService.validateAndExtractClaims(token);

        assertEquals("user-123", claims.getSubject());
        assertEquals(TEST_ISSUER, claims.getIssuer());
        assertEquals("john.doe", claims.get("username", String.class));
        assertNotNull(claims.getId());
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }

    // -------------------------------------------------------------------------
    // Token Validation Tests
    // -------------------------------------------------------------------------

    @Test
    public void shouldValidateValidToken() {
        FtgoUserContext userContext = createTestUserContext();
        String token = jwtTokenService.generateAccessToken(userContext);

        assertTrue(jwtTokenService.isTokenValid(token));
    }

    @Test
    public void shouldRejectExpiredToken() {
        // Create a service with very short expiration
        FtgoJwtProperties shortExpiryProperties = new FtgoJwtProperties();
        shortExpiryProperties.setSecret(TEST_SECRET);
        shortExpiryProperties.setIssuer(TEST_ISSUER);
        shortExpiryProperties.setExpirationSeconds(0); // expires immediately
        shortExpiryProperties.setClockSkewSeconds(0);   // no clock skew tolerance
        JwtTokenService shortExpiryService = new JwtTokenService(shortExpiryProperties);

        FtgoUserContext userContext = createTestUserContext();
        String token = shortExpiryService.generateAccessToken(userContext);

        // Token should be expired
        assertFalse(shortExpiryService.isTokenValid(token));
    }

    @Test(expected = ExpiredJwtException.class)
    public void shouldThrowExpiredJwtExceptionForExpiredToken() {
        FtgoJwtProperties shortExpiryProperties = new FtgoJwtProperties();
        shortExpiryProperties.setSecret(TEST_SECRET);
        shortExpiryProperties.setIssuer(TEST_ISSUER);
        shortExpiryProperties.setExpirationSeconds(0);
        shortExpiryProperties.setClockSkewSeconds(0);
        JwtTokenService shortExpiryService = new JwtTokenService(shortExpiryProperties);

        FtgoUserContext userContext = createTestUserContext();
        String token = shortExpiryService.generateAccessToken(userContext);

        shortExpiryService.validateAndExtractClaims(token);
    }

    @Test(expected = MalformedJwtException.class)
    public void shouldThrowMalformedJwtExceptionForInvalidToken() {
        jwtTokenService.validateAndExtractClaims("not.a.valid.jwt.token");
    }

    @Test(expected = SignatureException.class)
    public void shouldThrowSignatureExceptionForTamperedToken() {
        FtgoUserContext userContext = createTestUserContext();
        String token = jwtTokenService.generateAccessToken(userContext);

        // Tamper with the token by modifying the last character
        String tamperedToken = token.substring(0, token.length() - 1) +
                (token.endsWith("A") ? "B" : "A");

        jwtTokenService.validateAndExtractClaims(tamperedToken);
    }

    @Test
    public void shouldRejectTokenWithDifferentSigningKey() {
        FtgoJwtProperties differentKeyProperties = new FtgoJwtProperties();
        differentKeyProperties.setSecret("a-completely-different-secret-key-at-least-32-bytes!!");
        differentKeyProperties.setIssuer(TEST_ISSUER);
        JwtTokenService differentKeyService = new JwtTokenService(differentKeyProperties);

        FtgoUserContext userContext = createTestUserContext();
        String token = differentKeyService.generateAccessToken(userContext);

        // Validating with the original service should fail
        assertFalse(jwtTokenService.isTokenValid(token));
    }

    @Test
    public void shouldRejectInvalidTokenFormat() {
        assertFalse(jwtTokenService.isTokenValid(""));
        assertFalse(jwtTokenService.isTokenValid("garbage"));
        assertFalse(jwtTokenService.isTokenValid("a.b.c"));
    }

    // -------------------------------------------------------------------------
    // Claims Extraction Tests
    // -------------------------------------------------------------------------

    @Test
    public void shouldExtractUserContext() {
        FtgoUserContext originalContext = FtgoUserContext.builder()
                .userId("user-456")
                .username("jane.smith")
                .roles(Arrays.asList("USER", "MANAGER"))
                .permissions(Arrays.asList("order:read", "restaurant:manage"))
                .build();

        String token = jwtTokenService.generateAccessToken(originalContext);
        Claims claims = jwtTokenService.validateAndExtractClaims(token);
        FtgoUserContext extractedContext = jwtTokenService.extractUserContext(claims);

        assertEquals("user-456", extractedContext.getUserId());
        assertEquals("jane.smith", extractedContext.getUsername());
        assertTrue(extractedContext.hasRole("USER"));
        assertTrue(extractedContext.hasRole("MANAGER"));
        assertTrue(extractedContext.hasPermission("order:read"));
        assertTrue(extractedContext.hasPermission("restaurant:manage"));
    }

    @Test
    public void shouldExtractUserContextWithEmptyRolesAndPermissions() {
        FtgoUserContext originalContext = FtgoUserContext.builder()
                .userId("user-789")
                .username("no.roles")
                .roles(Collections.<String>emptyList())
                .permissions(Collections.<String>emptyList())
                .build();

        String token = jwtTokenService.generateAccessToken(originalContext);
        Claims claims = jwtTokenService.validateAndExtractClaims(token);
        FtgoUserContext extractedContext = jwtTokenService.extractUserContext(claims);

        assertEquals("user-789", extractedContext.getUserId());
        assertTrue(extractedContext.getRoles().isEmpty());
        assertTrue(extractedContext.getPermissions().isEmpty());
    }

    @Test
    public void shouldExtractTokenIdAndIssuer() {
        FtgoUserContext userContext = createTestUserContext();

        String token = jwtTokenService.generateAccessToken(userContext);
        Claims claims = jwtTokenService.validateAndExtractClaims(token);
        FtgoUserContext extractedContext = jwtTokenService.extractUserContext(claims);

        assertNotNull(extractedContext.getTokenId());
        assertEquals(TEST_ISSUER, extractedContext.getIssuer());
    }

    // -------------------------------------------------------------------------
    // Token Refresh Tests
    // -------------------------------------------------------------------------

    @Test
    public void shouldRefreshAccessToken() {
        FtgoUserContext userContext = createTestUserContext();
        TokenPair originalTokens = jwtTokenService.generateTokenPair(userContext);

        TokenPair refreshedTokens = jwtTokenService.refreshAccessToken(originalTokens.getRefreshToken());

        assertNotNull(refreshedTokens);
        assertNotEquals(originalTokens.getAccessToken(), refreshedTokens.getAccessToken());
        assertEquals(originalTokens.getRefreshToken(), refreshedTokens.getRefreshToken());
        assertEquals("Bearer", refreshedTokens.getTokenType());
    }

    @Test
    public void shouldPreserveUserClaimsAfterRefresh() {
        FtgoUserContext originalContext = FtgoUserContext.builder()
                .userId("user-refresh")
                .username("refresh.user")
                .roles(Arrays.asList("ADMIN"))
                .permissions(Arrays.asList("all:manage"))
                .build();

        TokenPair originalTokens = jwtTokenService.generateTokenPair(originalContext);
        TokenPair refreshedTokens = jwtTokenService.refreshAccessToken(originalTokens.getRefreshToken());

        Claims refreshedClaims = jwtTokenService.validateAndExtractClaims(refreshedTokens.getAccessToken());
        FtgoUserContext refreshedContext = jwtTokenService.extractUserContext(refreshedClaims);

        assertEquals("user-refresh", refreshedContext.getUserId());
        assertEquals("refresh.user", refreshedContext.getUsername());
        assertTrue(refreshedContext.hasRole("ADMIN"));
        assertTrue(refreshedContext.hasPermission("all:manage"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldRejectAccessTokenAsRefreshToken() {
        FtgoUserContext userContext = createTestUserContext();
        TokenPair tokens = jwtTokenService.generateTokenPair(userContext);

        // Try to use the access token as a refresh token
        jwtTokenService.refreshAccessToken(tokens.getAccessToken());
    }

    @Test
    public void shouldIdentifyRefreshToken() {
        FtgoUserContext userContext = createTestUserContext();
        TokenPair tokens = jwtTokenService.generateTokenPair(userContext);

        assertTrue(jwtTokenService.isRefreshToken(tokens.getRefreshToken()));
        assertFalse(jwtTokenService.isRefreshToken(tokens.getAccessToken()));
    }

    // -------------------------------------------------------------------------
    // Configuration Tests
    // -------------------------------------------------------------------------

    @Test(expected = IllegalArgumentException.class)
    public void shouldRejectSecretShorterThan32Bytes() {
        FtgoJwtProperties shortKeyProperties = new FtgoJwtProperties();
        shortKeyProperties.setSecret("too-short");
        new JwtTokenService(shortKeyProperties);
    }

    // -------------------------------------------------------------------------
    // Helper Methods
    // -------------------------------------------------------------------------

    private FtgoUserContext createTestUserContext() {
        return FtgoUserContext.builder()
                .userId("test-user-1")
                .username("testuser")
                .roles(Arrays.asList("USER"))
                .permissions(Arrays.asList("order:read"))
                .build();
    }
}
