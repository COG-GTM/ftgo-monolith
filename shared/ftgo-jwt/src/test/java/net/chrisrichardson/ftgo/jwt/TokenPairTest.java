package net.chrisrichardson.ftgo.jwt;

import net.chrisrichardson.ftgo.jwt.model.TokenPair;
import org.junit.Test;

import java.time.Instant;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link TokenPair}.
 */
public class TokenPairTest {

    @Test
    public void shouldCreateTokenPair() {
        Instant expiresAt = Instant.now().plusSeconds(3600);
        Instant refreshExpiresAt = Instant.now().plusSeconds(86400);

        TokenPair tokenPair = new TokenPair(
                "access-token", "refresh-token", "Bearer",
                expiresAt, refreshExpiresAt);

        assertEquals("access-token", tokenPair.getAccessToken());
        assertEquals("refresh-token", tokenPair.getRefreshToken());
        assertEquals("Bearer", tokenPair.getTokenType());
        assertEquals(expiresAt, tokenPair.getExpiresAt());
        assertEquals(refreshExpiresAt, tokenPair.getRefreshExpiresAt());
    }

    @Test
    public void shouldReportNotExpiredForFutureTokens() {
        Instant futureExpiry = Instant.now().plusSeconds(3600);
        TokenPair tokenPair = new TokenPair(
                "access", "refresh", "Bearer",
                futureExpiry, futureExpiry);

        assertFalse(tokenPair.isAccessTokenExpired());
        assertFalse(tokenPair.isRefreshTokenExpired());
    }

    @Test
    public void shouldReportExpiredForPastTokens() {
        Instant pastExpiry = Instant.now().minusSeconds(3600);
        TokenPair tokenPair = new TokenPair(
                "access", "refresh", "Bearer",
                pastExpiry, pastExpiry);

        assertTrue(tokenPair.isAccessTokenExpired());
        assertTrue(tokenPair.isRefreshTokenExpired());
    }

    @Test
    public void shouldImplementEqualsAndHashCode() {
        Instant expiry = Instant.now().plusSeconds(3600);
        TokenPair pair1 = new TokenPair("access", "refresh", "Bearer", expiry, expiry);
        TokenPair pair2 = new TokenPair("access", "refresh", "Bearer", expiry, expiry);

        assertEquals(pair1, pair2);
        assertEquals(pair1.hashCode(), pair2.hashCode());
    }

    @Test
    public void shouldHaveToString() {
        Instant expiry = Instant.now().plusSeconds(3600);
        TokenPair pair = new TokenPair("my-secret-access-jwt", "my-secret-refresh-jwt", "Bearer", expiry, expiry);

        String str = pair.toString();
        assertNotNull(str);
        assertTrue(str.contains("Bearer"));
        // Should NOT contain the actual token values (security)
        assertFalse(str.contains("my-secret-access-jwt"));
        assertFalse(str.contains("my-secret-refresh-jwt"));
    }

    @Test(expected = NullPointerException.class)
    public void shouldRejectNullAccessToken() {
        new TokenPair(null, "refresh", "Bearer", Instant.now(), Instant.now());
    }

    @Test(expected = NullPointerException.class)
    public void shouldRejectNullRefreshToken() {
        new TokenPair("access", null, "Bearer", Instant.now(), Instant.now());
    }

    @Test(expected = NullPointerException.class)
    public void shouldRejectNullTokenType() {
        new TokenPair("access", "refresh", null, Instant.now(), Instant.now());
    }
}
