package net.chrisrichardson.ftgo.security.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider(
                "test-secret-key-must-be-at-least-256-bits-long-for-hs256",
                900000,
                86400000,
                "ftgo-test"
        );
    }

    @Test
    void shouldGenerateAndValidateAccessToken() {
        String token = tokenProvider.generateAccessToken("user1", List.of("ADMIN", "CONSUMER"));

        assertTrue(tokenProvider.validateToken(token));
        assertEquals("user1", tokenProvider.getSubject(token));
    }

    @Test
    void shouldExtractRolesFromAccessToken() {
        List<String> roles = List.of("ADMIN", "CONSUMER");
        String token = tokenProvider.generateAccessToken("user1", roles);

        List<String> extractedRoles = tokenProvider.getRoles(token);

        assertNotNull(extractedRoles);
        assertEquals(2, extractedRoles.size());
        assertTrue(extractedRoles.contains("ADMIN"));
        assertTrue(extractedRoles.contains("CONSUMER"));
    }

    @Test
    void shouldGenerateRefreshToken() {
        String token = tokenProvider.generateRefreshToken("user1");

        assertTrue(tokenProvider.validateToken(token));
        assertEquals("user1", tokenProvider.getSubject(token));
    }

    @Test
    void shouldRejectInvalidToken() {
        assertFalse(tokenProvider.validateToken("invalid-token"));
    }

    @Test
    void shouldRejectNullToken() {
        assertFalse(tokenProvider.validateToken(null));
    }

    @Test
    void shouldRejectEmptyToken() {
        assertFalse(tokenProvider.validateToken(""));
    }

    @Test
    void shouldRejectTamperedToken() {
        String token = tokenProvider.generateAccessToken("user1", List.of("ADMIN"));
        String tamperedToken = token + "tampered";

        assertFalse(tokenProvider.validateToken(tamperedToken));
    }

    @Test
    void shouldParseTokenClaims() {
        String token = tokenProvider.generateAccessToken("user1", List.of("ADMIN"));
        var claims = tokenProvider.parseToken(token);

        assertEquals("user1", claims.getSubject());
        assertEquals("ftgo-test", claims.getIssuer());
        assertEquals("access", claims.get("type", String.class));
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }
}
